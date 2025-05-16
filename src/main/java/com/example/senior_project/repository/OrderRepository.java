package com.example.senior_project.repository;

import com.example.senior_project.model.Order;
import com.example.senior_project.model.Product;
import com.example.senior_project.model.User;
import com.example.senior_project.model.Offer;
import com.example.senior_project.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByBuyerOrderByCreatedAtDesc(User buyer);

    List<Order> findBySellerOrderByCreatedAtDesc(User seller);

    List<Order> findBySellerAndCreatedAtBetween(User seller, LocalDateTime startDate, LocalDateTime endDate);

    List<Order> findByProduct(Product product);

    long countByProduct(Product product);

    boolean existsByOffer(Offer offer);

    List<Order> findByBuyerAndStatusOrderByCreatedAtDesc(User buyer, OrderStatus status);
}