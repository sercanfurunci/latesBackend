package com.example.senior_project.repository;

import com.example.senior_project.model.Offer;
import com.example.senior_project.model.OfferStatus;
import com.example.senior_project.model.Product;
import com.example.senior_project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {
    List<Offer> findByBuyerOrderByCreatedAtDesc(User buyer);

    List<Offer> findByProductId(Long productId);

    List<Offer> findByBuyerAndProductAndStatus(User buyer, Product product, OfferStatus status);

}