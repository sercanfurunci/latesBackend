package com.example.senior_project.service.seller;

import com.example.senior_project.model.User;
import com.example.senior_project.model.UserType;
import com.example.senior_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SellerServiceImpl implements SellerService {
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<User> getFollowers(User seller) {
        if (seller.getUserType() != UserType.SELLER) {
            throw new RuntimeException("Only sellers can access their followers");
        }
        return seller.getFollowers().stream().toList();
    }
}