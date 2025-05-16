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
        log.info("Teklif oluşturma başladı - Ürün ID: {}, Alıcı: {}, Teklif Tutarı: {}",
                request.getProductId(), buyer.getEmail(), request.getOfferAmount());

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));

        validateOffer(product, buyer, request);

        Offer offer = createOffer(product, buyer, request);
        Offer savedOffer = offerRepository.save(offer);

        sendNotification(savedOffer);
        log.info("Teklif başarıyla oluşturuldu - Teklif ID: {}", savedOffer.getId());

        return savedOffer;
    }

    private void validateOffer(Product product, User buyer, OfferRequest request) {
        // Kullanıcı kontrolü
        if (buyer == null) {
            throw new RuntimeException("Kullanıcı bulunamadı");
        }

        // Ürün durumu kontrolü
        if (product.getStatus() != ProductStatus.AVAILABLE) {
            throw new RuntimeException("Ürün şu anda teklife açık değil");
        }

        // Stok kontrolü
        if (product.getStock() <= 0) {
            throw new RuntimeException("Ürün stokta yok");
        }

        // Kendi ürününe teklif verme kontrolü
        if (product.getSeller().getId().equals(buyer.getId())) {
            throw new RuntimeException("Kendi ürününüze teklif veremezsiniz");
        }

        // Teklif tutarı kontrolü
        if (request.getOfferAmount() == null) {
            throw new RuntimeException("Teklif tutarı boş olamaz");
        }

        if (request.getOfferAmount() <= 0) {
            throw new RuntimeException("Teklif tutarı 0'dan büyük olmalıdır");
        }

        // Ürün fiyatı kontrolü - Teklif tutarı ürün fiyatından büyük olamaz
        if (request.getOfferAmount() > product.getPrice()) {
            throw new RuntimeException(String.format(
                    "Teklif tutarı ürün fiyatından (%.2f TL) büyük olamaz",
                    product.getPrice()));
        }

        // Aktif teklif kontrolü
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
        try {
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
            log.info("Bildirim gönderildi - Teklif ID: {}", offer.getId());
        } catch (Exception e) {
            log.error("Bildirim gönderme hatası: {}", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<Offer> getBuyerOffers(User buyer) {
        if (buyer == null) {
            throw new RuntimeException("Kullanıcı bulunamadı");
        }
        return offerRepository.findByBuyerOrderByCreatedAtDesc(buyer);
    }

    @Transactional
    public void cancelOffer(Long offerId, User buyer) {
        log.info("Teklif iptal işlemi başladı - Teklif ID: {}, Alıcı: {}", offerId, buyer.getEmail());

        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Teklif bulunamadı"));

        if (!offer.getBuyer().equals(buyer)) {
            throw new RuntimeException("Bu teklifi iptal etme yetkiniz yok");
        }

        if (offer.getStatus() != OfferStatus.PENDING) {
            throw new RuntimeException("Sadece bekleyen teklifler iptal edilebilir");
        }

        offer.setStatus(OfferStatus.CANCELLED);
        offerRepository.save(offer);

        log.info("Teklif başarıyla iptal edildi - Teklif ID: {}", offerId);
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