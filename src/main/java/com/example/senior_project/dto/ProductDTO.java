package com.example.senior_project.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.example.senior_project.model.ProductStatus;
import com.example.senior_project.model.ProductType;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private String condition;
    private List<String> images;
    private Long sellerId;
    private String sellerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isActive;
    private boolean isAvailable;
    private int stock;
    private double rating;
    private int reviewCount;
    private ProductStatus status;
    private Set<String> tags;
    private Long categoryId;
    private String categoryName;
    private String ingredients;
    private String preparationTime;
    private String shippingDetails;
    private ProductType type;
}