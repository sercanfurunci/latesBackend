package com.example.senior_project.controller.seller;

import com.example.senior_project.model.User;
import com.example.senior_project.service.seller.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/seller")
@RequiredArgsConstructor
public class SellerController {
    private final SellerService sellerService;

    @GetMapping("/followers")
    public ResponseEntity<List<User>> getFollowers(@AuthenticationPrincipal User seller) {
        return ResponseEntity.ok(sellerService.getFollowers(seller));
    }
}