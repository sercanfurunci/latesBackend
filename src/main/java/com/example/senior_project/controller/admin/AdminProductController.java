package com.example.senior_project.controller.admin;

import com.example.senior_project.dto.ProductDTO;
import com.example.senior_project.dto.ProductStatusUpdateRequest;
import com.example.senior_project.model.Product;
import com.example.senior_project.service.admin.AdminProductService;
import com.example.senior_project.service.DtoConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {
    private final AdminProductService adminProductService;
    private final DtoConverter dtoConverter;

    @GetMapping
    public ResponseEntity<Page<Product>> getAllProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return ResponseEntity.ok(adminProductService.getProductsByCategoryAndSearch(category, search, pageable));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProductDetails(@PathVariable Long productId) {
        return ResponseEntity.ok(adminProductService.getProductDetails(productId));
    }

    @PutMapping("/{productId}/status")
    public ResponseEntity<Product> updateProductStatus(
            @PathVariable Long productId,
            @Valid @RequestBody ProductStatusUpdateRequest request) {
        return ResponseEntity.ok(adminProductService.updateProductStatus(productId, request));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long productId,
            @RequestParam(required = false) String reason) {
        adminProductService.deleteProduct(productId, reason);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/pending")
    public ResponseEntity<Page<Product>> getPendingProducts(Pageable pageable) {
        return ResponseEntity.ok(adminProductService.getPendingProducts(pageable));
    }

    @PostMapping("/{productId}/approve")
    public ResponseEntity<Product> approveProduct(
            @PathVariable Long productId,
            @RequestParam(required = false) String message) {
        return ResponseEntity.ok(adminProductService.approveProduct(productId, message));
    }

    @PostMapping("/{productId}/reject")
    public ResponseEntity<Product> rejectProduct(
            @PathVariable Long productId,
            @RequestBody(required = false) ProductStatusUpdateRequest request) {
        return ResponseEntity
                .ok(adminProductService.rejectProduct(productId, request != null ? request.getMessage() : null));
    }

    @PostMapping("/{productId}/images")
    public ResponseEntity<?> uploadProductImages(
            @PathVariable Long productId,
            @RequestParam("images") List<MultipartFile> images) {
        try {
            adminProductService.addProductImages(productId, images);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload images: " + e.getMessage());
        }
    }
}