package com.example.senior_project.dto;

import com.example.senior_project.model.Product;
import lombok.Data;

@Data
public class CartResponse {
    private Long id;
    private Product product;
    private Integer quantity;
    private Double price;
    private Long acceptedOfferId;
}