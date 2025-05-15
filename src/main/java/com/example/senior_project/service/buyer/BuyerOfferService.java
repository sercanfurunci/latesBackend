package com.example.senior_project.service.buyer;

import com.example.senior_project.dto.NotificationRequest;
import com.example.senior_project.dto.OfferRequest;
import com.example.senior_project.model.*;
import com.example.senior_project.repository.OfferRepository;
import com.example.senior_project.repository.ProductRepository;
import com.example.senior_project.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BuyerOfferService {
    private final OfferRepository offerRepository;
    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    public Offer makeOffer(OfferRequest request, User buyer) {
        try {
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));

            validateOffer(product, buyer, request);

            Offer offer = createOffer(product, buyer, request);
            Offer savedOffer = offerRepository.save(offer);

            sendNotification(savedOffer);

            return savedOffer;
        } catch (Exception e) {
            throw new RuntimeException("Teklif oluşturulurken bir hata oluştu: " + e.getMessage());
        }
    }

    private void validateOffer(Product product, User buyer, OfferRequest request) {
        if (product.getStatus() != ProductStatus.AVAILABLE) {
            throw new RuntimeException("Ürün şu anda teklife açık değil");
        }

        if (product.getStock() <= 0) {
            throw new RuntimeException("Ürün stokta yok");
        }

        if (product.getSeller().getId().equals(buyer.getId())) {
            throw new RuntimeException("Kendi ürününüze teklif veremezsiniz");
        }

        if (request.getOfferAmount() <= 0 || request.getOfferAmount() > product.getPrice()) {
            throw new RuntimeException("Geçersiz teklif tutarı");
        }

        List<Offer> activeOffers = offerRepository.findByBuyerAndProductAndStatus(
                buyer, product, OfferStatus.PENDING);
        if (!activeOffers.isEmpty()) {
            throw new RuntimeException("Bu ürün için zaten aktif bir teklifiniz var");
        }
    }

    private Offer createOffer(Product product, User buyer, OfferRequest request) {
        return Offer.builder()
                .buyer(buyer)
                .product(product)
                .offerAmount(request.getOfferAmount())
                .message(request.getMessage())
                .status(OfferStatus.PENDING)
                .build();
    }

    private void sendNotification(Offer offer) {
        NotificationRequest request = NotificationRequest.builder()
                .user(offer.getProduct().getSeller())
                .type(NotificationType.NEW_OFFER)
                .message(String.format("%s %s ürününüz için %.2f TL teklif verdi",
                        offer.getBuyer().getFirstName(),
                        offer.getBuyer().getLastName(),
                        offer.getOfferAmount()))
                .link("/offers/" + offer.getId())
                .build();

        notificationService.createNotification(request);
    }

    @Transactional(readOnly = true)
    public List<Offer> getBuyerOffers(User buyer) {
        return offerRepository.findByBuyerOrderByCreatedAtDesc(buyer);
    }

    @Transactional
    public void cancelOffer(Long offerId, User buyer) {
        try {
            Offer offer = offerRepository.findById(offerId)
                    .orElseThrow(() -> new RuntimeException("Teklif bulunamadı"));

            if (!offer.getBuyer().equals(buyer)) {
                throw new RuntimeException("Bu teklifi iptal etme yetkiniz yok");
            }

            if (offer.getStatus() == OfferStatus.ACCEPTED) {
                throw new RuntimeException("Kabul edilmiş teklifler iptal edilemez");
            }

            offer.setStatus(OfferStatus.CANCELLED);
            offerRepository.save(offer);

            log.info("Teklif iptal edildi. Teklif ID: {}, Alıcı: {}", offerId, buyer.getEmail());
        } catch (Exception e) {
            log.error("Teklif iptal edilirken hata: {}", e.getMessage());
            throw new RuntimeException("Teklif iptal edilirken bir hata oluştu: " + e.getMessage());
        }
    }

    public Offer getOfferById(Long offerId, User buyer) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Teklif bulunamadı"));
        if (!offer.getBuyer().getId().equals(buyer.getId())) {
            throw new RuntimeException("Yetkisiz erişim");
        }
        return offer;
    }
}