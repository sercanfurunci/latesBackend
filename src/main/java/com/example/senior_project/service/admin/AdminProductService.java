package com.example.senior_project.service.admin;

import com.example.senior_project.dto.ProductReviewRequest;
import com.example.senior_project.dto.ProductUpdateRequest;
import com.example.senior_project.dto.ProductStatusUpdateRequest;
import com.example.senior_project.model.Category;
import com.example.senior_project.model.Product;
import com.example.senior_project.model.ProductStatus;
import com.example.senior_project.model.NotificationType;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        public Product updateProductStatus(Long productId, ProductStatusUpdateRequest request) {
                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));

                product.setStatus(request.getStatus());
                Product updatedProduct = productRepository.save(product);

                // Send notification to seller
                notificationService.notifySeller(
                                product.getSeller(),
                                String.format("'%s' ürününün durumu '%s' olarak güncellendi. Sebep: %s",
                                                product.getTitle(),
                                                request.getStatus(),
                                                request.getMessage()),
                                product);

                return updatedProduct;
        }

        @Transactional
        public void deleteProduct(Long productId, String reason) {
                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));

                // Send notification to seller before deleting
                notificationService.notifySeller(
                                product.getSeller(),
                                String.format("'%s' ürünü silindi. Sebep: %s",
                                                product.getTitle(),
                                                reason != null ? reason : "Belirtilmedi"),
                                product);

                productRepository.delete(product);
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
                notificationService.createSystemNotification(
                                seller,
                                String.format("Ürününüz '%s' başarıyla onaylandı. %s", product.getTitle(),
                                                message != null ? message : ""));

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
                notificationService.createSystemNotification(
                                seller,
                                String.format("Ürününüz '%s' reddedildi. Sebep: %s", product.getTitle(),
                                                message != null && !message.isEmpty() ? message : "Belirtilmedi"));

                return product;
        }

        @Transactional
        public void addProductImages(Long productId, List<MultipartFile> images) {
                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));

                // Mevcut resimleri koru ve yeni resimleri ekle
                List<String> currentImages = product.getImages();
                if (currentImages == null) {
                        currentImages = new ArrayList<>();
                }

                // Yeni resimleri ekle
                for (MultipartFile image : images) {
                        if (!image.isEmpty()) {
                                try {
                                        String fileName = UUID.randomUUID().toString() + "_"
                                                        + image.getOriginalFilename();
                                        String uploadDir = "uploads/products/" + productId;
                                        File directory = new File(uploadDir);
                                        if (!directory.exists()) {
                                                directory.mkdirs();
                                        }
                                        File destFile = new File(
                                                        directory.getAbsolutePath() + File.separator + fileName);
                                        image.transferTo(destFile);
                                        currentImages.add("/uploads/products/" + productId + "/" + fileName);
                                } catch (IOException e) {
                                        throw new RuntimeException(
                                                        "Resim yüklenirken bir hata oluştu: " + e.getMessage());
                                }
                        }
                }

                product.setImages(currentImages);
                productRepository.save(product);
        }
}