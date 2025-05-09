package com.example.senior_project.repository;

import com.example.senior_project.model.Product;
import com.example.senior_project.model.ProductStatus;
import com.example.senior_project.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
        Page<Product> findByCategoryIdAndTitleContainingIgnoreCase(Long categoryId, String title, Pageable pageable);

        Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

        Page<Product> findByTitleContainingIgnoreCase(String title, Pageable pageable);

        Page<Product> findByStatus(ProductStatus status, Pageable pageable);

        Page<Product> findByStatusNot(ProductStatus status, Pageable pageable);

        List<Product> findByStatusNotAndTitleContainingOrDescriptionContaining(ProductStatus status, String title,
                        String description);

        List<Product> findByStatusNotAndCategoryId(ProductStatus status, Long categoryId);

        List<Product> findByStatusNotAndPriceBetween(ProductStatus status, Double minPrice, Double maxPrice);

        List<Product> findByTitleContainingOrDescriptionContaining(String title, String description);

        List<Product> findByCategoryId(Long categoryId);

        List<Product> findByPriceBetween(Double minPrice, Double maxPrice);

        List<Product> findBySeller(User seller);

        Page<Product> findByCategoryIdAndTitleContainingOrDescriptionContaining(Long categoryId, String title,
                        String description, Pageable pageable);

        Page<Product> findByTitleContainingOrDescriptionContaining(String title, String description, Pageable pageable);

        List<Product> findBySellerOrderByCreatedAtDesc(User seller);

        List<Product> findBySellerOrderByPriceAsc(User seller);

        List<Product> findBySellerOrderByPriceDesc(User seller);
}