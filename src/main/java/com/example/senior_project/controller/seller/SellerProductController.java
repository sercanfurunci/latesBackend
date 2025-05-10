package com.example.senior_project.controller.seller;

import com.example.senior_project.dto.ProductCreateRequest;
import com.example.senior_project.dto.ProductUpdateRequest;
import com.example.senior_project.dto.ProductDTO;
import com.example.senior_project.model.Product;
import com.example.senior_project.model.User;
import com.example.senior_project.service.seller.SellerProductService;
import com.example.senior_project.service.DtoConverter;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/seller/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
public class SellerProductController {
    private final SellerProductService sellerProductService;
    private final DtoConverter dtoConverter;

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(
            @Valid @RequestBody ProductCreateRequest request,
            @AuthenticationPrincipal User seller) {
        Product product = sellerProductService.createProduct(request, seller);
        return ResponseEntity.ok(dtoConverter.toProductDTO(product));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductUpdateRequest request,
            @AuthenticationPrincipal User seller) {
        try {
            Product updatedProduct = sellerProductService.updateProduct(productId, request, seller);
            return ResponseEntity.ok(dtoConverter.toProductDTO(updatedProduct));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long productId,
            @AuthenticationPrincipal User seller) {
        sellerProductService.deleteProduct(productId, seller);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<Product>> getSellerProducts(
            @AuthenticationPrincipal User seller,
            @RequestParam(required = false, defaultValue = "newest") String sortBy) {
        return ResponseEntity.ok(sellerProductService.getSellerProducts(seller, sortBy));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProductDetails(
            @PathVariable Long productId,
            @AuthenticationPrincipal User seller) {
        return ResponseEntity.ok(sellerProductService.getProductDetails(productId, seller));
    }

    @PostMapping("/{productId}/images")
    public ResponseEntity<?> uploadImages(
            @PathVariable Long productId,
            @RequestParam("images") List<MultipartFile> images,
            @AuthenticationPrincipal User seller) {
        try {
            log.debug("Resim yükleme isteği alındı - Ürün ID: {}, Satıcı: {}", productId, seller.getEmail());
            log.debug("Resim sayısı: {}", images.size());

            Product updatedProduct = sellerProductService.uploadImages(productId, images, seller);
            return ResponseEntity.ok(dtoConverter.toProductDTO(updatedProduct));
        } catch (RuntimeException e) {
            log.error("Resim yükleme hatası: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("Beklenmeyen hata: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Resim yüklenirken bir hata oluştu: " + e.getMessage());
        }
    }
}