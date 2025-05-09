package com.example.senior_project.controller.buyer;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.senior_project.model.Favorite;
import com.example.senior_project.model.User;
import com.example.senior_project.service.buyer.FavoriteService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/buyer")
@RequiredArgsConstructor
public class FavoriteController {
    private final FavoriteService favoriteService;

    @GetMapping("/favorites")
    public ResponseEntity<List<ProductDTO>> getFavorites() {
        return ResponseEntity.ok(favoriteService.getFavorites());
    }

    @PostMapping("/favorites/toggle/{productId}")
    public ResponseEntity<Void> toggleFavorite(@PathVariable Long productId) {
        favoriteService.toggleFavorite(productId);
        return ResponseEntity.ok().build();
    }
}