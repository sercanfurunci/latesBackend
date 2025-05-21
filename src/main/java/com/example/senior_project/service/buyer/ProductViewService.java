package com.example.senior_project.service.buyer;

import com.example.senior_project.model.Product;
import com.example.senior_project.model.ProductStatus;
import com.example.senior_project.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductViewService {
    private final ProductRepository productRepository;

    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findByStatus(ProductStatus.AVAILABLE, pageable);
    }

    public List<Product> searchProducts(String keyword) {
        return productRepository.findByStatusAndTitleContainingOrDescriptionContaining(
                ProductStatus.AVAILABLE, keyword, keyword);
    }

    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByStatusAndCategoryId(ProductStatus.AVAILABLE, categoryId);
    }

    public List<Product> filterByPriceRange(Double minPrice, Double maxPrice) {
        return productRepository.findByStatusAndPriceBetween(ProductStatus.AVAILABLE, minPrice, maxPrice);
    }

    public Product getProductDetails(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));

        if (product.getStatus() != ProductStatus.AVAILABLE) {
            throw new RuntimeException("Ürün şu anda satışta değil");
        }

        return product;
    }
}