package com.example.senior_project.service.seller;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.example.senior_project.model.Order;
import com.example.senior_project.model.OrderStatus;
import com.example.senior_project.model.User;
import com.example.senior_project.repository.OrderRepository;
import com.example.senior_project.service.NotificationService;

@Service
@RequiredArgsConstructor
public class SellerOrderService {
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    public List<Order> getSellerOrders(User seller) {
        return orderRepository.findBySellerOrderByCreatedAtDesc(seller);
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus, User seller) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getSeller().equals(seller)) {
            throw new RuntimeException("Not authorized to update this order");
        }

        order.setStatus(newStatus);
        if (newStatus == OrderStatus.SHIPPED
                && (order.getTrackingNumber() == null || order.getTrackingNumber().isEmpty())) {
            String autoTracking = "TRK" + System.currentTimeMillis();
            order.setTrackingNumber(autoTracking);
        }
        Order savedOrder = orderRepository.save(order);

        String message = String.format("Your order status has been updated to %s", newStatus);
        notificationService.notifyBuyer(order.getBuyer(), message, order);

        return savedOrder;
    }

    public Order getOrderDetails(Long orderId, User seller) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getSeller().equals(seller)) {
            throw new RuntimeException("Not authorized to view this order");
        }

        return order;
    }
}