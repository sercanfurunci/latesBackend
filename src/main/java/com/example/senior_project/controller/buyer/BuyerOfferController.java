package com.example.senior_project.controller.buyer;

import com.example.senior_project.dto.OfferRequest;
import com.example.senior_project.model.Offer;
import com.example.senior_project.model.User;
import com.example.senior_project.service.buyer.BuyerOfferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/buyer/offers")
@RequiredArgsConstructor
public class BuyerOfferController {
    private final BuyerOfferService buyerOfferService;

    @PostMapping
    public ResponseEntity<Offer> makeOffer(
            @Valid @RequestBody OfferRequest request,
            @AuthenticationPrincipal User buyer) {
        return ResponseEntity.ok(buyerOfferService.makeOffer(request, buyer));
    }

    @GetMapping("/my-offers")
    public ResponseEntity<List<Offer>> getBuyerOffers(@AuthenticationPrincipal User buyer) {
        return ResponseEntity.ok(buyerOfferService.getBuyerOffers(buyer));
    }

    @PostMapping("/{offerId}/cancel")
    public ResponseEntity<Void> cancelOffer(
            @PathVariable Long offerId,
            @AuthenticationPrincipal User buyer) {
        buyerOfferService.cancelOffer(offerId, buyer);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{offerId}")
    public ResponseEntity<Offer> getOfferById(
            @PathVariable Long offerId,
            @AuthenticationPrincipal User buyer) {
        Offer offer = buyerOfferService.getOfferById(offerId, buyer);
        return ResponseEntity.ok(offer);
    }
}