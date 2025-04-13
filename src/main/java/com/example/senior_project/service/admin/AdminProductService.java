package com.example.senior_project.service.admin;

import com.example.senior_project.dto.ProductReviewRequest;
import com.example.senior_project.dto.ProductUpdateRequest;
import com.example.senior_project.model.Category;
import com.example.senior_project.model.Product;
import com.example.senior_project.model.ProductStatus;
import com.example.senior_project.model.User;
import com.example.senior_project.repository.CategoryRepository;
import com.example.senior_project.repository.ProductRepository;
import com.example.senior_project.repository.UserRepository;
import com.example.senior_project.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminProductService {
        private final ProductRepository productRepository;
        private final UserRepository userRepository;
        private final NotificationService notificationService;
        private final CategoryRepository categoryRepository;

        public Page<Product> getProductsByCategoryAndSearch(String category, String search, Pageable pageable) {
                if (category != null && !category.equals("ALL")) {
                        if (search != null && !search.isEmpty()) {
                                return productRepository.findByCategoryIdAndTitleContainingIgnoreCase(
                                                Long.parseLong(category), search, pageable);
                        }
                        return productRepository.findByCategoryId(Long.parseLong(category), pageable);
                }
                if (search != null && !search.isEmpty()) {
                        return productRepository.findByTitleContainingIgnoreCase(search, pageable);
                }
                return productRepository.findAll(pageable);
        }

        public Product getProductDetails(Long productId) {
                return productRepository.findById(productId)
                                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));
        }

        @Transactional
        public Product updateProduct(Long productId, ProductUpdateRequest request) {
                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));

                if (request.getTitle() != null) {
                        product.setTitle(request.getTitle());
                }
                if (request.getPrice() != null) {
                        product.setPrice(request.getPrice());
                }
                if (request.getStock() != null) {
                        product.setStock(request.getStock());
                }
                if (request.getStatus() != null) {
                        product.setStatus(request.getStatus());
                }
                if (request.getCategoryId() != null) {
                        Category category = categoryRepository.findById(request.getCategoryId())
                                        .orElseThrow(() -> new RuntimeException("Kategori bulunamadı"));
                        product.setCategory(category);
                }

                return productRepository.save(product);
        }

        @Transactional
        public Product updateProductStatus(Long productId, String message) {
                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));

                // Mevcut durumun tersini al
                ProductStatus newStatus = product.getStatus() == ProductStatus.AVAILABLE
                                ? ProductStatus.INACTIVE
                                : ProductStatus.AVAILABLE;

                product.setStatus(newStatus);
                product = productRepository.save(product);

                // Satıcıya bildirim gönder
                User seller = product.getSeller();
                notificationService.sendNotification(
                                seller.getId(),
                                "Ürün Durumu Güncellendi",
                                "Ürününüz '" + product.getTitle() + "' durumu güncellendi. " + message);

                return product;
        }

        @Transactional
        public void deleteProduct(Long productId, String reason) {
                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));

                productRepository.delete(product);

                // Satıcıya bildirim gönder
                User seller = product.getSeller();
                notificationService.sendNotification(
                                seller.getId(),
                                "Ürün Silindi",
                                "Ürününüz '" + product.getTitle() + "' admin tarafından silindi. Sebep: " + reason);
        }

        public Page<Product> getPendingProducts(Pageable pageable) {
                return productRepository.findByStatus(ProductStatus.PENDING_REVIEW, pageable);
        }

        @Transactional
        public Product approveProduct(Long productId, String message) {
                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));

                product.setStatus(ProductStatus.AVAILABLE);
                product = productRepository.save(product);

                // Satıcıya bildirim gönder
                User seller = product.getSeller();
                notificationService.sendNotification(
                                seller.getId(),
                                "Ürün Onaylandı",
                                "Ürününüz '" + product.getTitle() + "' başarıyla onaylandı. " + message);

                return product;
        }

        @Transactional
        public Product rejectProduct(Long productId, String message) {
                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));

                product.setStatus(ProductStatus.REJECTED);
                product = productRepository.save(product);

                // Satıcıya bildirim gönder
                User seller = product.getSeller();
                notificationService.sendNotification(
                                seller.getId(),
                                "Ürün Reddedildi",
                                "Ürününüz '" + product.getTitle() + "' reddedildi. " + message);

                return product;
        }
}