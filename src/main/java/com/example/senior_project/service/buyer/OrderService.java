package com.example.senior_project.service.buyer;

import com.example.senior_project.dto.OrderRequest;
import com.example.senior_project.model.*;
import com.example.senior_project.repository.OfferRepository;
import com.example.senior_project.repository.OrderRepository;
import com.example.senior_project.repository.ProductRepository;
import com.example.senior_project.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OfferRepository offerRepository;
    private final NotificationService notificationService;

    @Transactional
    public Order createOrder(OrderRequest request, User buyer) {
        try {
            Offer offer = offerRepository.findById(request.getOfferId())
                    .orElseThrow(() -> new RuntimeException("Teklif bulunamadı"));

            // Teklif durumunu kontrol et
            if (offer.getStatus() != OfferStatus.ACCEPTED) {
                throw new RuntimeException("Sadece kabul edilmiş teklifler için sipariş oluşturulabilir");
            }

            // Bu teklif için daha önce sipariş oluşturulmuş mu kontrol et
            if (orderRepository.existsByOffer(offer)) {
                throw new RuntimeException("Bu teklif için zaten bir sipariş oluşturulmuş");
            }

            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));

            if (!product.getStatus().equals(ProductStatus.AVAILABLE)) {
                throw new RuntimeException("Ürün satın alınamaz durumda");
            }

            Order order = Order.builder()
                    .buyer(buyer)
                    .seller(product.getSeller())
                    .product(product)
                    .offer(offer)
                    .status(OrderStatus.PENDING)
                    .shippingStatus(ShippingStatus.PREPARING)
                    .shippingAddress(request.getShippingAddress())
                    .finalPrice(offer.getOfferAmount())
                    .totalAmount(offer.getOfferAmount()) // Şimdilik aynı, kargo ücreti eklenebilir
                    .build();

            Order savedOrder = orderRepository.save(order);

            // Ürün durumunu ve stoğunu güncelle
            product.setStock(product.getStock() - 1); // veya sipariş miktarı kadar azalt
            if (product.getStock() <= 0) {
                product.setStatus(ProductStatus.SOLD);
            } else {
                product.setStatus(ProductStatus.AVAILABLE);
            }
            productRepository.save(product);

            // Satıcıya bildirim gönder
            notificationService.notifySeller(
                    product.getSeller(),
                    "Yeni bir siparişiniz var: " + product.getTitle(),
                    savedOrder);

            return savedOrder;

        } catch (Exception e) {
            log.error("Sipariş oluşturulurken hata: {}", e.getMessage());
            throw new RuntimeException("Sipariş oluşturulurken bir hata oluştu: " + e.getMessage());
        }
    }

    public List<Order> getBuyerOrders(User buyer) {
        return orderRepository.findByBuyerOrderByCreatedAtDesc(buyer);
    }

    public Order getOrderDetails(Long orderId, User buyer) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getBuyer().equals(buyer)) {
            throw new RuntimeException("Not authorized to view this order");
        }

        return order;
    }

    @Transactional
    public void cancelOrder(Long orderId, User buyer) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Sipariş bulunamadı"));

            if (!order.getBuyer().equals(buyer)) {
                throw new RuntimeException("Bu siparişi iptal etme yetkiniz yok");
            }

            if (!order.getStatus().equals(OrderStatus.PENDING)) {
                throw new RuntimeException("Sadece bekleyen siparişler iptal edilebilir");
            }

            // Sipariş durumunu güncelle
            order.setStatus(OrderStatus.CANCELLED);
            order.setShippingStatus(ShippingStatus.CANCELLED);
            orderRepository.save(order);

            // Ürünü tekrar satışa çıkar
            Product product = order.getProduct();
            product.setStatus(ProductStatus.AVAILABLE);
            productRepository.save(product);

            // Log ekle
            log.info("Sipariş iptal edildi. Sipariş ID: {}, Alıcı: {}", orderId, buyer.getEmail());

        } catch (Exception e) {
            log.error("Sipariş iptal edilirken hata: {}", e.getMessage());
            throw new RuntimeException("Sipariş iptal edilirken bir hata oluştu: " + e.getMessage());
        }
    }
}