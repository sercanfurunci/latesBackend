// src/main/java/com/example/senior_project/repository/CartRepository.java
package com.example.senior_project.repository;

import com.example.senior_project.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    List<Cart> findByUserId(Long userId);

    Optional<Cart> findByUserIdAndProductId(Long userId, Long productId);

    void deleteByUserIdAndProductId(Long userId, Long productId);

    @Query("SELECT SUM(c.price * c.quantity) FROM Cart c WHERE c.user.id = :userId")
    Double getTotalAmount(Long userId);
}