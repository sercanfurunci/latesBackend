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

import java.util.List;

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
            // Debug için log ekleyelim
            System.out.println("Update request received:");
            System.out.println("Product ID: " + productId);
            System.out.println("Seller ID: " + seller.getId());
            System.out.println("Request Data: " + request.toString());

            // Önce ürünün var olduğunu ve satıcıya ait olduğunu kontrol edelim
            Product existingProduct = sellerProductService.getProductDetails(productId, seller);

            if (!existingProduct.getSeller().getId().equals(seller.getId())) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body("Bu ürün size ait değil");
            }

            Product updatedProduct = sellerProductService.updateProduct(productId, request, seller);
            return ResponseEntity.ok(dtoConverter.toProductDTO(updatedProduct));

        } catch (RuntimeException e) {
            System.out.println("Error in updateProduct: " + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error in updateProduct: " + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ürün güncellenirken bir hata oluştu");
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
    public ResponseEntity<Product> uploadImages(
            @PathVariable Long productId,
            @RequestParam("images") List<MultipartFile> images,
            @AuthenticationPrincipal User seller) {
        return ResponseEntity.ok(sellerProductService.uploadImages(productId, images, seller));
    }
}