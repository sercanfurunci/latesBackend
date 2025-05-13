package com.example.senior_project.service.buyer;

import com.example.senior_project.model.Favorite;
import com.example.senior_project.model.Product;
import com.example.senior_project.model.User;
import com.example.senior_project.repository.FavoriteRepository;
import com.example.senior_project.repository.ProductRepository;
import com.example.senior_project.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    @Transactional
    public Favorite addToFavorites(Long productId, User user) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));

        if (favoriteRepository.existsByUserAndProduct(user, product)) {
            throw new RuntimeException("Bu ürün zaten favorilerinizde");
        }

        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setProduct(product);
        Favorite savedFavorite = favoriteRepository.save(favorite);

        // Bildirim gönder
        if (!user.getId().equals(product.getSeller().getId())) {
            notificationService.sendProductFavoritedNotification(user, product.getSeller(), product.getTitle(),
                    product.getId());
        }

        return savedFavorite;
    }

    @Transactional
    public void removeFromFavorites(Long productId, User user) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));

        if (!favoriteRepository.existsByUserAndProduct(user, product)) {
            throw new RuntimeException("Bu ürün favorilerinizde değil");
        }

        favoriteRepository.deleteByUserAndProduct(user, product);
    }

    @Transactional(readOnly = true)
    public List<Favorite> getUserFavorites(User user) {
        return favoriteRepository.findByUserOrderByCreatedAtDesc(user);
    }
}