package com.example.senior_project.dto;

import lombok.Data;

@Data
public class CartRequest {
    private Long productId;
    private Integer quantity = 1; // Varsayılan değer 1
}