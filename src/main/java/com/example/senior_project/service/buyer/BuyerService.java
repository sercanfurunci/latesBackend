package com.example.senior_project.service.buyer;

import com.example.senior_project.model.User;
import com.example.senior_project.model.UserType;
import com.example.senior_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BuyerService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<User> getFollowing(User buyer) {
        return buyer.getFollowing().stream().toList();
    }

    @Transactional
    public void followSeller(Long sellerId, User buyer) {
        if (buyer.getUserType() != UserType.BUYER) {
            throw new RuntimeException("Only buyers can follow sellers");
        }

        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        if (seller.getUserType() != UserType.SELLER) {
            throw new RuntimeException("Can only follow sellers");
        }

        if (buyer.getFollowing().contains(seller)) {
            throw new RuntimeException("Already following this seller");
        }

        buyer.getFollowing().add(seller);
        seller.getFollowers().add(buyer);

        // Update follower counts
        Integer sellerFollowerCount = seller.getFollowerCount();
        if (sellerFollowerCount == null)
            sellerFollowerCount = 0;
        seller.setFollowerCount(sellerFollowerCount + 1);
        Integer buyerFollowingCount = buyer.getFollowingCount();
        if (buyerFollowingCount == null)
            buyerFollowingCount = 0;
        buyer.setFollowingCount(buyerFollowingCount + 1);

        userRepository.save(buyer);
        userRepository.save(seller);
    }

    @Transactional
    public void unfollowSeller(Long sellerId, User buyer) {
        if (buyer.getUserType() != UserType.BUYER) {
            throw new RuntimeException("Only buyers can unfollow sellers");
        }

        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        if (!buyer.getFollowing().contains(seller)) {
            throw new RuntimeException("Not following this seller");
        }

        buyer.getFollowing().remove(seller);
        seller.getFollowers().remove(buyer);

        // Update follower counts
        Integer sellerFollowerCount = seller.getFollowerCount();
        if (sellerFollowerCount == null)
            sellerFollowerCount = 0;
        seller.setFollowerCount(Math.max(0, sellerFollowerCount - 1));
        Integer buyerFollowingCount = buyer.getFollowingCount();
        if (buyerFollowingCount == null)
            buyerFollowingCount = 0;
        buyer.setFollowingCount(Math.max(0, buyerFollowingCount - 1));

        userRepository.save(buyer);
        userRepository.save(seller);
    }
}