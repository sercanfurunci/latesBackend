// src/main/java/com/example/senior_project/controller/CartController.java
package com.example.senior_project.controller;

import com.example.senior_project.dto.CartRequest; // Import ekleyelim
import com.example.senior_project.dto.CartResponse;
import com.example.senior_project.model.Cart;
import com.example.senior_project.model.User;
import com.example.senior_project.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping
    public ResponseEntity<List<CartResponse>> getCartItems(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(cartService.getCartResponses(user));
    }

    @PostMapping("/add")
    public ResponseEntity<Cart> addToCart(
            @AuthenticationPrincipal User user,
            @RequestBody CartRequest request) {
        return ResponseEntity.ok(cartService.addToCart(user, request.getProductId(), request.getQuantity()));
    }

    @PutMapping("/update/{productId}")
    public ResponseEntity<Cart> updateQuantity(
            @AuthenticationPrincipal User user,
            @PathVariable Long productId,
            @RequestBody java.util.Map<String, Integer> body) {
        Integer quantity = body.get("quantity");
        Cart updatedCart = cartService.updateQuantity(user, productId, quantity);
        return updatedCart != null ? ResponseEntity.ok(updatedCart) : ResponseEntity.ok().build();
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<Void> removeFromCart(
            @AuthenticationPrincipal User user,
            @PathVariable Long productId) {
        cartService.removeFromCart(user, productId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/total")
    public ResponseEntity<Double> getTotalAmount(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(cartService.getTotalAmount(user));
    }
}