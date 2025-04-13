package com.example.senior_project.service.seller;

import com.example.senior_project.model.Category;
import com.example.senior_project.model.Product;
import com.example.senior_project.model.User;
import com.example.senior_project.model.ProductStatus;
import com.example.senior_project.repository.CategoryRepository;
import com.example.senior_project.repository.ProductRepository;
import com.example.senior_project.dto.ProductCreateRequest;
import com.example.senior_project.dto.ProductUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SellerProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private static final Logger log = LoggerFactory.getLogger(SellerProductService.class);

    @Transactional
    public Product createProduct(ProductCreateRequest request, User seller) {
        try {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));

            Product product = Product.builder()
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .price(request.getPrice())
                    .stock(request.getStock())
                    .category(category)
                    .seller(seller)
                    .status(ProductStatus.AVAILABLE)
                    .images(new ArrayList<>(request.getImages()))
                    .tags(new HashSet<>(request.getTags()))
                    .ingredients(request.getIngredients())
                    .preparationTime(request.getPreparationTime())
                    .shippingDetails(request.getShippingDetails())
                    .type(request.getType())
                    .build();

            return productRepository.save(product);
        } catch (Exception e) {
            throw new RuntimeException("Ürün oluşturulurken bir hata oluştu: " + e.getMessage());
        }
    }

    @Transactional
    public Product updateProduct(Long productId, ProductUpdateRequest request, User seller) {
        try {
            log.debug("Updating product with ID: {} for seller: {}", productId, seller.getEmail());
            log.debug("Update request: {}", request);

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));

            log.debug("Found product: {}", product);
            log.debug("Product seller ID: {}, Request seller ID: {}",
                    product.getSeller().getId(), seller.getId());

            if (!product.getSeller().getId().equals(seller.getId())) {
                log.warn("Unauthorized update attempt by seller: {} for product: {}",
                        seller.getEmail(), productId);
                throw new RuntimeException("Bu ürünü düzenleme yetkiniz yok");
            }

            if (request.getCategoryId() != null) {
                Category category = categoryRepository.findById(request.getCategoryId())
                        .orElseThrow(() -> new RuntimeException("Kategori bulunamadı"));
                product.setCategory(category);
            }

            // Update fields if they are not null
            if (request.getTitle() != null && !request.getTitle().trim().isEmpty())
                product.setTitle(request.getTitle().trim());
            if (request.getDescription() != null && !request.getDescription().trim().isEmpty())
                product.setDescription(request.getDescription().trim());
            if (request.getPrice() != null && request.getPrice() > 0)
                product.setPrice(request.getPrice());
            if (request.getStock() != null && request.getStock() >= 0)
                product.setStock(request.getStock());
            if (request.getStatus() != null)
                product.setStatus(request.getStatus());
            if (request.getShippingDetails() != null && !request.getShippingDetails().trim().isEmpty())
                product.setShippingDetails(request.getShippingDetails().trim());

            log.debug("Product updated successfully: {}", product);
            return productRepository.save(product);
        } catch (Exception e) {
            log.error("Error updating product: {}", e.getMessage(), e);
            throw new RuntimeException("Ürün güncellenirken bir hata oluştu: " + e.getMessage());
        }
    }

    @Transactional
    public void deleteProduct(Long productId, User seller) {
        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));

            if (!product.getSeller().getId().equals(seller.getId())) {
                throw new RuntimeException("Bu ürünü silme yetkiniz yok");
            }

            productRepository.delete(product);
        } catch (Exception e) {
            throw new RuntimeException("Ürün silinirken bir hata oluştu: " + e.getMessage());
        }
    }

    public List<Product> getSellerProducts(User seller) {
        return productRepository.findBySeller(seller);
    }

    public Product getProductDetails(Long productId, User seller) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.getSeller().equals(seller)) {
            throw new RuntimeException("Not authorized to view this product");
        }

        return product;
    }

    @Transactional
    public Product uploadImages(Long productId, List<MultipartFile> images, User seller) {
        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));

            if (!product.getSeller().getId().equals(seller.getId())) {
                throw new RuntimeException("Bu ürüne resim ekleme yetkiniz yok");
            }

            List<String> imageUrls = new ArrayList<>();
            String uploadDir = "uploads/products/" + productId;
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            for (MultipartFile image : images) {
                String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
                String filePath = uploadDir + "/" + fileName;

                // Resmi kaydet
                File dest = new File(filePath);
                image.transferTo(dest);

                // Resim URL'sini listeye ekle
                imageUrls.add(fileName);
            }

            // Mevcut resimlere yeni resimleri ekle
            product.getImages().addAll(imageUrls);
            return productRepository.save(product);
        } catch (Exception e) {
            throw new RuntimeException("Resimler yüklenirken bir hata oluştu: " + e.getMessage());
        }
    }
}