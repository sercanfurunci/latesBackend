package com.example.senior_project.service;

import com.example.senior_project.model.ChatMessage;
import com.example.senior_project.model.Order;
import com.example.senior_project.model.OrderStatus;
import com.example.senior_project.model.User;
import com.example.senior_project.model.Product;
import com.example.senior_project.repository.OrderRepository;
import com.example.senior_project.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;

@Service
@RequiredArgsConstructor
public class ChatBotService {
    private static final Logger logger = LoggerFactory.getLogger(ChatBotService.class);
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final Optional<RestTemplate> restTemplate;
    private final Optional<ObjectMapper> objectMapper;

    @Value("${openai.api.key:}")
    private String openaiApiKey;

    @Value("${openai.api.url:}")
    private String openaiApiUrl;

    @Value("${chatbot.mode:AI}")
    private String chatbotMode;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    // Cache for product data
    private Map<Long, Product> productCache = new HashMap<>();
    private LocalDateTime lastProductCacheUpdate;

    // Cache for user orders
    private Map<Long, List<Order>> userOrdersCache = new HashMap<>();
    private LocalDateTime lastOrdersCacheUpdate;

    @Scheduled(fixedRate = 300000) // Update cache every 5 minutes
    public void updateCaches() {
        logger.info("Updating chatbot caches...");
        updateProductCache();
        updateOrdersCache();
    }

    private void updateProductCache() {
        try {
            List<Product> products = productRepository.findAll();
            Map<Long, Product> newCache = new HashMap<>();
            for (Product product : products) {
                newCache.put(product.getId(), product);
            }
            productCache = newCache;
            lastProductCacheUpdate = LocalDateTime.now();
            logger.info("Product cache updated with {} products", products.size());
        } catch (Exception e) {
            logger.error("Error updating product cache", e);
        }
    }

    private void updateOrdersCache() {
        try {
            userOrdersCache.clear();
            lastOrdersCacheUpdate = LocalDateTime.now();
            logger.info("Orders cache cleared");
        } catch (Exception e) {
            logger.error("Error updating orders cache", e);
        }
    }

    public ChatMessage processMessage(ChatMessage message, User user) {
        logger.info("Processing message from user {}: {}", user.getEmail(), message.getText());
        String response;
        try {
            if ("AI".equalsIgnoreCase(chatbotMode) && restTemplate.isPresent() && objectMapper.isPresent()
                    && openaiApiKey != null && !openaiApiKey.isEmpty()) {
                // AI response with retry logic
                response = getAIResponseWithRetry(message.getText(), prepareContext(user));
            } else {
                // Fallback: Rule-based response
                logger.info("Using rule-based response for user {}", user.getEmail());
                response = generateRuleBasedResponse(message, user);
            }
        } catch (Exception e) {
            logger.error("Error processing message for user {}: {}", user.getEmail(), e.getMessage());
            response = generateRuleBasedResponse(message, user);
        }

        ChatMessage botResponse = new ChatMessage();
        botResponse.setText(response);
        botResponse.setSender("bot");
        botResponse.setTimestamp(LocalDateTime.now());
        botResponse.setMessageType("text");

        logger.info("Bot response to user {}: {}", user.getEmail(), response);
        return botResponse;
    }

    private String getAIResponseWithRetry(String userMessage, String context) {
        int retryCount = 0;
        while (retryCount < MAX_RETRIES) {
            try {
                return getAIResponse(userMessage, context);
            } catch (Exception e) {
                retryCount++;
                logger.warn("AI response attempt {} failed: {}", retryCount, e.getMessage());
                if (retryCount < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        throw new RuntimeException("Failed to get AI response after " + MAX_RETRIES + " attempts");
    }

    private String prepareContext(User user) {
        StringBuilder context = new StringBuilder();
        context.append("Kullanıcı Bilgileri:\n");
        context.append("Ad: ").append(user.getFirstName()).append("\n");
        context.append("Soyad: ").append(user.getLastName()).append("\n");
        context.append("E-posta: ").append(user.getEmail()).append("\n\n");

        // Get orders from cache or database
        List<Order> userOrders = getUserOrders(user);
        if (!userOrders.isEmpty()) {
            context.append("Son Siparişler:\n");
            for (Order order : userOrders) {
                context.append("Sipariş No: ").append(order.getId()).append("\n");
                context.append("Durum: ").append(getOrderStatusInTurkish(order.getStatus())).append("\n");
                context.append("Tarih: ").append(order.getCreatedAt().format(formatter)).append("\n");
                context.append("Tutar: ").append(order.getTotalAmount()).append(" TL\n");
                if (order.getTrackingNumber() != null && !order.getTrackingNumber().isEmpty()) {
                    context.append("Kargo Takip No: ").append(order.getTrackingNumber()).append("\n");
                }
                context.append("\n");
            }
        }

        // Get products from cache or database
        List<Product> products = getAllProducts();
        if (!products.isEmpty()) {
            context.append("Ürünler:\n");
            for (Product product : products) {
                context.append("Ürün ID: ").append(product.getId()).append("\n");
                context.append("Ürün Adı: ").append(product.getTitle()).append("\n");
                context.append("Açıklama: ").append(product.getDescription()).append("\n");
                context.append("Stok: ").append(product.getStock()).append("\n");
                context.append("Fiyat: ").append(product.getPrice()).append(" TL\n");
                if (product.getSeller() != null) {
                    context.append("Satıcı: ").append(product.getSeller().getFirstName()).append(" ")
                            .append(product.getSeller().getLastName()).append("\n");
                    if (product.getSeller().getPhoneNumber() != null) {
                        context.append("Satıcı Telefon: ").append(product.getSeller().getPhoneNumber()).append("\n");
                    }
                }
                if (product.getCategory() != null) {
                    context.append("Kategori: ").append(product.getCategory().getName()).append("\n");
                }
                context.append("Durum: ").append(product.getStatus()).append("\n");
                context.append("---\n");
            }
        }

        return context.toString();
    }

    private List<Order> getUserOrders(User user) {
        try {
            if (lastOrdersCacheUpdate != null &&
                    lastOrdersCacheUpdate.plusMinutes(5).isAfter(LocalDateTime.now()) &&
                    userOrdersCache.containsKey(user.getId())) {
                return userOrdersCache.get(user.getId());
            }
            List<Order> orders = orderRepository.findByBuyerOrderByCreatedAtDesc(user);
            userOrdersCache.put(user.getId(), orders);
            return orders;
        } catch (Exception e) {
            logger.error("Error getting user orders: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<Product> getAllProducts() {
        try {
            if (lastProductCacheUpdate != null &&
                    lastProductCacheUpdate.plusMinutes(5).isAfter(LocalDateTime.now())) {
                return new ArrayList<>(productCache.values());
            }
            return productRepository.findAll();
        } catch (Exception e) {
            logger.error("Error getting all products: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private String getAIResponse(String userMessage, String context) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openaiApiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-3.5-turbo");

            List<Map<String, String>> messages = new ArrayList<>();

            // Sistem mesajı
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", "Sen bir e-ticaret asistanısın. Kullanıcıya siparişleri, " +
                    "kargo durumu, iade işlemleri ve diğer konularda yardımcı oluyorsun. " +
                    "Ürün sorgularında, verilen ürün bilgilerini kullanarak doğru ve detaylı yanıtlar ver. " +
                    "Stok durumu, fiyat ve ürün detayları gibi bilgileri içeren yanıtlar oluştur. " +
                    "Satıcı telefon numaraları profillerde görünür olduğu için, kullanıcı satıcı telefon numarası sorduğunda bu bilgiyi paylaşabilirsin. "
                    +
                    "Yanıtlarını Türkçe olarak ver. Kullanıcı bilgileri ve sipariş geçmişi şu şekilde:\n" + context);
            messages.add(systemMessage);

            // Kullanıcı mesajı
            Map<String, String> userMessageMap = new HashMap<>();
            userMessageMap.put("role", "user");
            userMessageMap.put("content", userMessage);
            messages.add(userMessageMap);

            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 500);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            Map<String, Object> response = restTemplate.get().postForObject(
                    openaiApiUrl,
                    request,
                    Map.class);

            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    Map<String, String> message = (Map<String, String>) choice.get("message");
                    return message.get("content");
                }
            }

            throw new RuntimeException("AI yanıtı alınamadı");
        } catch (Exception e) {
            throw new RuntimeException("AI servisi ile iletişim hatası: " + e.getMessage());
        }
    }

    private String getOrderStatusInTurkish(OrderStatus status) {
        switch (status) {
            case PENDING:
                return "Beklemede";
            case CONFIRMED:
                return "Onaylandı";
            case SHIPPED:
                return "Kargoya Verildi";
            case DELIVERED:
                return "Teslim Edildi";
            case CANCELLED:
                return "İptal Edildi";
            case REFUNDED:
                return "İade Edildi";
            default:
                return status.toString();
        }
    }

    private String generateRuleBasedResponse(ChatMessage message, User user) {
        String text = message.getText().toLowerCase();
        String userName = user.getFirstName();

        // Sipariş durumu sorgusu
        if (text.contains("sipariş") || text.contains("durum") || text.contains("takip")) {
            List<Order> userOrders = getUserOrders(user);
            if (userOrders.isEmpty()) {
                return "Merhaba " + userName + ", henüz bir siparişiniz bulunmamaktadır.";
            }
            Order latestOrder = userOrders.get(0);
            String response = String.format(
                    "Merhaba %s, son siparişinizin durumu: %s. Sipariş numaranız %d ve %s tarihinde verilmiş olup tutarı %.2f TL'dir.",
                    userName,
                    getOrderStatusInTurkish(latestOrder.getStatus()),
                    latestOrder.getId(),
                    latestOrder.getCreatedAt().format(formatter),
                    latestOrder.getTotalAmount());

            if (latestOrder.getTrackingNumber() != null && !latestOrder.getTrackingNumber().isEmpty()) {
                response += String.format(" Kargo takip numaranız: %s", latestOrder.getTrackingNumber());
            }

            return response;
        }

        // Kargo takip numarası sorgusu
        if (text.contains("kargo") || text.contains("takip") || text.contains("kod")) {
            List<Order> shippedOrders = orderRepository.findByBuyerAndStatusOrderByCreatedAtDesc(user,
                    OrderStatus.SHIPPED);
            if (shippedOrders.isEmpty()) {
                return "Merhaba " + userName + ", şu anda kargoda olan bir siparişiniz bulunmamaktadır.";
            }
            Order shippedOrder = shippedOrders.get(0);
            String trackingNumber = shippedOrder.getTrackingNumber();
            if (trackingNumber == null || trackingNumber.isEmpty()) {
                return String.format(
                        "Merhaba %s, siparişiniz kargoya verilmiş ancak henüz takip numarası oluşturulmamış. Lütfen birkaç saat sonra tekrar deneyin.",
                        userName);
            }
            return String.format(
                    "Merhaba %s, son kargoya verilen siparişinizin takip numarası: %s. Sipariş numaranız: %d", userName,
                    trackingNumber, shippedOrder.getId());
        }

        // Diğer yanıtlar...
        if (text.contains("merhaba") || text.contains("selam")) {
            return "Merhaba " + userName + "! Size nasıl yardımcı olabilirim?";
        }
        if (text.contains("teşekkür") || text.contains("sağol")) {
            return "Rica ederim " + userName + "! Başka bir sorunuz olursa yardımcı olmaktan mutluluk duyarım.";
        }
        if (text.contains("iade") || text.contains("değişim")) {
            return String.format(
                    "Merhaba %s, iade ve değişim işlemleri için sipariş detay sayfasındaki 'İade Talebi' butonunu kullanabilirsiniz. 14 gün içinde ücretsiz iade hakkınız bulunmaktadır.",
                    userName);
        }
        if (text.contains("ödeme") || text.contains("fiyat")) {
            return String.format(
                    "Merhaba %s, ödeme seçeneklerimiz: Kredi kartı, havale/EFT ve kapıda ödeme. Taksit seçenekleri için kredi kartı ile ödeme yapabilirsiniz.",
                    userName);
        }
        if (text.contains("ürün") || text.contains("stok")) {
            return String.format(
                    "Merhaba %s, ürünlerimiz hakkında detaylı bilgi için ürün sayfasını ziyaret edebilirsiniz. Stok durumu ürün sayfasında belirtilmektedir.",
                    userName);
        }
        return String.format(
                "Merhaba %s, üzgünüm bu konuda size yardımcı olamıyorum. Lütfen müşteri hizmetlerimizle iletişime geçin veya başka bir konuda yardım isteyin.",
                userName);
    }
}