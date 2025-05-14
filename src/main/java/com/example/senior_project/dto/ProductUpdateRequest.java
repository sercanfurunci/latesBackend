package com.example.senior_project.dto;

import java.util.List;

import com.example.senior_project.model.ProductStatus;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@Data
public class ProductUpdateRequest {
    @Size(min = 3, max = 100, message = "Başlık 3-100 karakter arasında olmalıdır")
    private String title;

    @Size(min = 10, max = 2000, message = "Açıklama 10-2000 karakter arasında olmalıdır")
    private String description;

    @Min(value = 0, message = "Fiyat 0'dan büyük olmalıdır")
    private Double price;

    private Long categoryId;

    @Min(value = 0, message = "Stok 0'dan küçük olamaz")
    private Integer stock;

    private List<String> images;
    private List<String> tags;
    private String shippingDetails;
    private ProductStatus status;
}