package com.example.senior_project.service.seller;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.example.senior_project.model.Order;
import com.example.senior_project.model.OrderStatus;
import com.example.senior_project.model.ShippingStatus;
import com.example.senior_project.model.User;
import com.example.senior_project.repository.OrderRepository;
import com.example.senior_project.service.NotificationService;

@Service
@RequiredArgsConstructor
@Slf4j
public class SellerOrderService {
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    public List<Order> getSellerOrders(User seller) {
        return orderRepository.findBySellerOrderByCreatedAtDesc(seller);
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus, User seller) {
        log.info("Sipariş durumu güncelleniyor - Sipariş ID: {}, Yeni Durum: {}, Satıcı: {}",
                orderId, newStatus, seller.getEmail());

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Sipariş bulunamadı"));

        if (!order.getSeller().equals(seller)) {
            throw new RuntimeException("Bu siparişi güncelleme yetkiniz yok");
        }

        // Sipariş durumuna göre işlem yap
        switch (newStatus) {
            case CONFIRMED:
                // Sipariş onaylandığında sadece durumu güncelle
                order.setStatus(OrderStatus.CONFIRMED);
                order.setShippingStatus(ShippingStatus.PREPARING);
                log.info("Sipariş onaylandı ve hazırlanıyor - Sipariş ID: {}", orderId);
                break;

            case SHIPPED:
                // Sipariş kargoya verildiğinde takip numarası oluştur
                if (order.getStatus() != OrderStatus.CONFIRMED) {
                    throw new RuntimeException("Sipariş önce onaylanmalıdır");
                }
                String autoTracking = "TRK" + System.currentTimeMillis();
                order.setTrackingNumber(autoTracking);
                order.setStatus(OrderStatus.SHIPPED);
                order.setShippingStatus(ShippingStatus.SHIPPED);
                log.info("Sipariş kargoya verildi - Takip No: {}", autoTracking);
                break;

            default:
                order.setStatus(newStatus);
                break;
        }

        Order savedOrder = orderRepository.save(order);

        // Bildirim mesajını duruma göre özelleştir
        String message;
        switch (newStatus) {
            case CONFIRMED:
                message = "Siparişiniz onaylandı ve hazırlanıyor.";
                break;
            case SHIPPED:
                message = String.format("Siparişiniz kargoya verildi. Takip No: %s", order.getTrackingNumber());
                break;
            default:
                message = String.format("Siparişinizin durumu güncellendi: %s", newStatus);
        }

        notificationService.notifyBuyer(order.getBuyer(), message, order);

        return savedOrder;
    }

    public Order getOrderDetails(Long orderId, User seller) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Sipariş bulunamadı"));

        if (!order.getSeller().equals(seller)) {
            throw new RuntimeException("Bu siparişi görüntüleme yetkiniz yok");
        }

        return order;
    }
}