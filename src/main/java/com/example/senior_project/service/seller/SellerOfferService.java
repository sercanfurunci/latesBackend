package com.example.senior_project.service.seller;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.util.List;

import com.example.senior_project.model.Offer;
import com.example.senior_project.model.User;
import com.example.senior_project.repository.OfferRepository;
import com.example.senior_project.service.NotificationService;
import com.example.senior_project.model.OfferStatus;
import com.example.senior_project.model.NotificationType;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SellerOfferService {
    private final OfferRepository offerRepository;
    private final NotificationService notificationService;

    public List<Offer> getProductOffers(Long productId, User seller) {
        return offerRepository.findByProductId(productId);
    }

    @Transactional(noRollbackFor = Exception.class)
    public Offer acceptOffer(Long offerId, User seller) {
        try {
            Offer offer = offerRepository.findById(offerId)
                    .orElseThrow(() -> new RuntimeException("Teklif bulunamadı"));

            if (!offer.getProduct().getSeller().equals(seller)) {
                throw new RuntimeException("Bu teklifi kabul etme yetkiniz yok");
            }

            offer.setStatus(OfferStatus.ACCEPTED);
            Offer savedOffer = offerRepository.save(offer);

            notificationService.sendOfferNotification(savedOffer, NotificationType.OFFER_ACCEPTED);

            return savedOffer;
        } catch (Exception e) {
            log.error("Teklif kabul edilirken hata: {}", e.getMessage());
            throw new RuntimeException("Teklif kabul edilirken bir hata oluştu");
        }
    }

    @Transactional(noRollbackFor = Exception.class)
    public Offer rejectOffer(Long offerId, User seller) {
        try {
            Offer offer = offerRepository.findById(offerId)
                    .orElseThrow(() -> new RuntimeException("Teklif bulunamadı"));

            if (!offer.getProduct().getSeller().equals(seller)) {
                throw new RuntimeException("Bu teklifi reddetme yetkiniz yok");
            }

            offer.setStatus(OfferStatus.REJECTED);
            Offer savedOffer = offerRepository.save(offer);

            notificationService.sendOfferNotification(savedOffer, NotificationType.OFFER_REJECTED);

            return savedOffer;
        } catch (Exception e) {
            log.error("Teklif reddedilirken hata: {}", e.getMessage());
            throw new RuntimeException("Teklif reddedilirken bir hata oluştu");
        }
    }

    public Offer getOfferById(Long offerId, User seller) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Teklif bulunamadı"));
        if (!offer.getProduct().getSeller().getId().equals(seller.getId())) {
            throw new RuntimeException("Yetkisiz erişim");
        }
        return offer;
    }
}