// src/main/java/com/example/senior_project/service/CartService.java
package com.example.senior_project.service;

import com.example.senior_project.model.Cart;
import com.example.senior_project.model.Product;
import com.example.senior_project.model.User;
import com.example.senior_project.model.Offer;
import com.example.senior_project.model.OfferStatus;
import com.example.senior_project.repository.CartRepository;
import com.example.senior_project.repository.ProductRepository;
import com.example.senior_project.repository.OfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final OfferRepository offerRepository;

    public List<Cart> getCartItems(User user) {
        return cartRepository.findByUserId(user.getId());
    }

    @Transactional
    public Cart addToCart(User user, Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));

        // Ürünün satıcısı kendisi olamaz
        if (product.getSeller().getId().equals(user.getId())) {
            throw new RuntimeException("Kendi ürününüzü sepete ekleyemezsiniz");
        }

        // Kullanıcının bu ürün için kabul edilmiş teklifi var mı?
        Offer acceptedOffer = offerRepository
                .findByBuyerAndProductAndStatus(user, product, OfferStatus.ACCEPTED)
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Bu ürün için kabul edilmiş teklifiniz yok!"));

        double offerPrice = acceptedOffer.getOfferAmount();

        Cart existingCartItem = cartRepository.findByUserIdAndProductId(user.getId(), productId)
                .orElse(null);

        if (existingCartItem != null) {
            existingCartItem.setQuantity(existingCartItem.getQuantity() + quantity);
            existingCartItem.setPrice(offerPrice); // Fiyatı güncelle
            return cartRepository.save(existingCartItem);
        }

        Cart cartItem = new Cart();
        cartItem.setUser(user);
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        cartItem.setPrice(offerPrice); // Teklif fiyatı

        return cartRepository.save(cartItem);
    }

    @Transactional
    public Cart updateQuantity(User user, Long productId, Integer quantity) {
        Cart cartItem = cartRepository.findByUserIdAndProductId(user.getId(), productId)
                .orElseThrow(() -> new RuntimeException("Sepette bu ürün bulunamadı"));

        if (quantity <= 0) {
            cartRepository.delete(cartItem);
            return null;
        }

        cartItem.setQuantity(quantity);
        return cartRepository.save(cartItem);
    }

    @Transactional
    public void removeFromCart(User user, Long productId) {
        cartRepository.deleteByUserIdAndProductId(user.getId(), productId);
    }

    public Double getTotalAmount(User user) {
        return cartRepository.getTotalAmount(user.getId());
    }
}