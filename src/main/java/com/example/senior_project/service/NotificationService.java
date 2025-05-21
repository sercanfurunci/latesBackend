package com.example.senior_project.service;

import com.example.senior_project.dto.NotificationRequest;
import com.example.senior_project.model.Notification;
import com.example.senior_project.model.NotificationType;
import com.example.senior_project.model.Offer;
import com.example.senior_project.model.Order;
import com.example.senior_project.model.Product;
import com.example.senior_project.model.User;
import com.example.senior_project.model.UserType;
import com.example.senior_project.repository.NotificationRepository;
import com.example.senior_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional(noRollbackFor = Exception.class)
    public void sendOfferNotification(Offer offer, NotificationType type) {
        try {
            String message = createOfferMessage(offer, type);
            NotificationRequest request = NotificationRequest.builder()
                    .user(offer.getBuyer())
                    .type(type)
                    .message(message)
                    .link("/offers/" + offer.getId())
                    .build();

            createNotification(request);
        } catch (Exception e) {
            log.error("Teklif bildirimi gönderilirken hata: {}", e.getMessage());
        }
    }

    @Transactional(noRollbackFor = Exception.class)
    public Notification createNotification(NotificationRequest request) {
        try {
            validateNotificationRequest(request);

            Notification notification = Notification.builder()
                    .user(request.getUser())
                    .title(request.getType().getDisplayName())
                    .message(request.getMessage())
                    .type(request.getType())
                    .link(request.getLink())
                    .isRead(false)
                    .build();

            return notificationRepository.save(notification);
        } catch (Exception e) {
            log.error("Bildirim oluşturulurken hata: {}", e.getMessage());
            return null;
        }
    }

    private void validateNotificationRequest(NotificationRequest request) {
        if (request.getUser() == null) {
            throw new IllegalArgumentException("Kullanıcı bilgisi eksik");
        }
        if (request.getType() == null) {
            throw new IllegalArgumentException("Bildirim tipi eksik");
        }
        if (!StringUtils.hasText(request.getMessage())) {
            throw new IllegalArgumentException("Bildirim mesajı eksik");
        }
    }

    private String createOfferMessage(Offer offer, NotificationType type) {
        String productName = offer.getProduct().getTitle();

        return switch (type) {
            case OFFER_ACCEPTED -> String.format("%s ürünü için verdiğiniz teklif kabul edildi", productName);
            case OFFER_REJECTED -> String.format("%s ürünü için verdiğiniz teklif reddedildi", productName);
            case NEW_OFFER -> String.format("%s ürünü için yeni bir teklif aldınız", productName);
            case OFFER_CANCELLED -> String.format("%s ürünü için teklif iptal edildi", productName);
            default -> throw new IllegalArgumentException("Geçersiz bildirim tipi: " + type);
        };
    }

    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndIsReadFalse(user);
    }

    public void markAsRead(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Bildirim bulunamadı"));

        if (!notification.getUser().equals(user)) {
            throw new RuntimeException("Bu bildirimi okuma yetkiniz yok");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    public void markAllAsRead(User user) {
        List<Notification> unreadNotifications = notificationRepository.findByUserAndIsReadFalse(user);
        unreadNotifications.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

    public void notifySeller(User seller, String title, String message, NotificationType type, String link) {
        NotificationRequest request = NotificationRequest.builder()
                .user(seller)
                .title(title)
                .message(message)
                .type(type)
                .link(link)
                .build();

        createNotification(request);
    }

    public void notifySeller(User seller, String message, Order order) {
        createNotification(NotificationRequest.builder()
                .user(seller)
                .type(NotificationType.ORDER_STATUS_CHANGE)
                .message(message)
                .link("/orders/" + order.getId())
                .build());
    }

    public void notifyNewMessage(User receiver, String message) {
        createNotification(NotificationRequest.builder()
                .user(receiver)
                .type(NotificationType.NEW_MESSAGE)
                .message(message)
                .build());
    }

    public void notifyBuyer(User buyer, String message, Offer offer) {
        createNotification(NotificationRequest.builder()
                .user(buyer)
                .type(NotificationType.OFFER_STATUS_CHANGE)
                .message(message)
                .link("/offers/" + offer.getId())
                .build());
    }

    public void notifyBuyer(User buyer, String message, Order order) {
        createNotification(NotificationRequest.builder()
                .user(buyer)
                .type(NotificationType.ORDER_STATUS_CHANGE)
                .message(message)
                .link("/orders/" + order.getId())
                .build());
    }

    public void createSystemNotification(User user, String message) {
        createNotification(NotificationRequest.builder()
                .user(user)
                .type(NotificationType.SYSTEM_MESSAGE)
                .message(message)
                .build());
    }

    public void notifySeller(User seller, String message, Product product) {
        createNotification(NotificationRequest.builder()
                .user(seller)
                .type(NotificationType.SYSTEM_MESSAGE)
                .message(message)
                .link("/products/" + product.getId())
                .build());
    }

    @Transactional
    public void sendNotification(Long userId, String title, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
    }

    @Transactional
    public void notifySeller(User seller, String message, Object data) {
        Notification notification = Notification.builder()
                .user(seller)
                .title("Ürün Bildirimi")
                .message(message)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
    }

    public void sendUserFollowNotification(User follower, User followedUser) {
        String message = String.format("%s %s seni takip etti.", follower.getFirstName(), follower.getLastName());
        String link = "/profile/" + follower.getId();
        createNotification(NotificationRequest.builder()
                .user(followedUser)
                .type(NotificationType.SYSTEM_MESSAGE)
                .message(message)
                .link(link)
                .build());
    }

    public void sendProductFavoritedNotification(User userWhoFavorited, User productOwner, String productName,
            Long productId) {
        String message = String.format("%s %s ürününü favorilere ekledi: %s",
                userWhoFavorited.getFirstName(), userWhoFavorited.getLastName(), productName);
        String link = "/products/" + productId;
        createNotification(NotificationRequest.builder()
                .user(productOwner)
                .type(NotificationType.SYSTEM_MESSAGE)
                .message(message)
                .link(link)
                .build());
    }

    public void notifyNewProduct(Product product) {
        try {
            // Tüm admin kullanıcılarını bul
            List<User> admins = userRepository.findByUserType(UserType.ADMIN);

            // Her admin için bildirim oluştur
            for (User admin : admins) {
                NotificationRequest request = NotificationRequest.builder()
                        .user(admin)
                        .type(NotificationType.PRODUCT_CREATED)
                        .message(String.format("Yeni ürün eklendi: %s - Satıcı: %s %s",
                                product.getTitle(),
                                product.getSeller().getFirstName(),
                                product.getSeller().getLastName()))
                        .link("/admin")
                        .build();

                createNotification(request);
            }

            // Satıcıya da bildirim gönder
            NotificationRequest sellerRequest = NotificationRequest.builder()
                    .user(product.getSeller())
                    .type(NotificationType.PRODUCT_CREATED)
                    .message(String.format("'%s' ürününüz admin onayı için gönderildi", product.getTitle()))
                    .link("/products/" + product.getId())
                    .build();

            createNotification(sellerRequest);
        } catch (Exception e) {
            log.error("Yeni ürün bildirimi gönderilirken hata: {}", e.getMessage());
        }
    }
}