package com.example.senior_project.dto;

import lombok.Data;

@Data
public class CommentRequest {
    private Long productId;
    private String content;
    private Integer rating;
}