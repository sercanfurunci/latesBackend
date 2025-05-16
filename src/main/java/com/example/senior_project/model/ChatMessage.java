package com.example.senior_project.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessage {
    private String text;
    private String sender;
    private LocalDateTime timestamp;
    private String messageType; // "text", "order", "product", etc.
    private Long orderId; // If message is about an order
    private Long productId; // If message is about a product
}