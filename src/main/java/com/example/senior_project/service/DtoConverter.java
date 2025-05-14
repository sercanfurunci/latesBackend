package com.example.senior_project.service;

import org.springframework.stereotype.Service;

import com.example.senior_project.dto.ProductDTO;
import com.example.senior_project.model.Product;
import java.math.BigDecimal;
import java.util.HashSet;

@Service
public class DtoConverter {
    public ProductDTO toProductDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice() != null ? BigDecimal.valueOf(product.getPrice()) : null)
                .stock(product.getStock() != null ? product.getStock() : 0)
                .status(product.getStatus())
                .images(product.getImages())
                .tags(product.getTags() != null ? new HashSet<>(product.getTags()) : null)
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .sellerId(product.getSeller() != null ? product.getSeller().getId() : null)
                .sellerName(product.getSeller() != null
                        ? (product.getSeller().getFirstName() + " " + product.getSeller().getLastName())
                        : null)
                .ingredients(product.getIngredients())
                .preparationTime(product.getPreparationTime())
                .shippingDetails(product.getShippingDetails())
                .type(product.getType())
                .build();
    }
}