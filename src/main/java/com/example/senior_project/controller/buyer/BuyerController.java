package com.example.senior_project.controller.buyer;

import com.example.senior_project.model.User;
import com.example.senior_project.model.Favorite;
import com.example.senior_project.service.buyer.BuyerService;
import com.example.senior_project.service.buyer.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/buyer")
@RequiredArgsConstructor
public class BuyerController {
    private final BuyerService buyerService;
    private final FavoriteService favoriteService;

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        } else {
            throw new RuntimeException("Principal is not an instance of User");
        }
    }

    @GetMapping("/following")
    public ResponseEntity<List<User>> getFollowing() {
        User buyer = getAuthenticatedUser();
        return ResponseEntity.ok(buyerService.getFollowing(buyer));
    }

    @PostMapping("/follow/{sellerId}")
    public ResponseEntity<Void> followSeller(@PathVariable Long sellerId) {
        User buyer = getAuthenticatedUser();
        buyerService.followSeller(sellerId, buyer);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/unfollow/{sellerId}")
    public ResponseEntity<Void> unfollowSeller(@PathVariable Long sellerId) {
        User buyer = getAuthenticatedUser();
        buyerService.unfollowSeller(sellerId, buyer);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<Favorite>> getFavorites() {
        User buyer = getAuthenticatedUser();
        return ResponseEntity.ok(favoriteService.getUserFavorites(buyer));
    }

    @PostMapping("/favorites/toggle/{productId}")
    public ResponseEntity<Void> toggleFavorite(@PathVariable Long productId) {
        User buyer = getAuthenticatedUser();
        try {
            favoriteService.addToFavorites(productId, buyer);
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Bu ürün zaten favorilerinizde")) {
                favoriteService.removeFromFavorites(productId, buyer);
            } else {
                throw e;
            }
        }
        return ResponseEntity.ok().build();
    }
}