package com.example.senior_project.controller.seller;

import com.example.senior_project.model.User;
import com.example.senior_project.service.seller.SellerAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/seller/analytics")
@RequiredArgsConstructor
public class SellerAnalyticsController {
    private final SellerAnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard(
            @AuthenticationPrincipal User seller,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(analyticsService.getSellerDashboard(seller, startDate, endDate));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<Map<String, Object>> getProductPerformance(
            @PathVariable Long productId,
            @AuthenticationPrincipal User seller) {
        return ResponseEntity.ok(analyticsService.getProductPerformance(productId, seller));
    }
}