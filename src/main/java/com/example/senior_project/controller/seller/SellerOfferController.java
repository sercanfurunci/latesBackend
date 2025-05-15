package com.example.senior_project.controller.seller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.senior_project.model.Offer;
import com.example.senior_project.model.User;
import com.example.senior_project.service.seller.SellerOfferService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/seller/offers")
@RequiredArgsConstructor
public class SellerOfferController {
    private final SellerOfferService sellerOfferService;

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Offer>> getProductOffers(
            @PathVariable Long productId,
            @AuthenticationPrincipal User seller) {
        return ResponseEntity.ok(sellerOfferService.getProductOffers(productId, seller));
    }

    @PostMapping("/{offerId}/accept")
    public ResponseEntity<Offer> acceptOffer(
            @PathVariable Long offerId,
            @AuthenticationPrincipal User seller) {
        return ResponseEntity.ok(sellerOfferService.acceptOffer(offerId, seller));
    }

    @PostMapping("/{offerId}/reject")
    public ResponseEntity<Offer> rejectOffer(
            @PathVariable Long offerId,
            @AuthenticationPrincipal User seller) {
        return ResponseEntity.ok(sellerOfferService.rejectOffer(offerId, seller));
    }

    @GetMapping("/{offerId}")
    public ResponseEntity<Offer> getOfferById(
            @PathVariable Long offerId,
            @AuthenticationPrincipal User seller) {
        Offer offer = sellerOfferService.getOfferById(offerId, seller);
        return ResponseEntity.ok(offer);
    }
}