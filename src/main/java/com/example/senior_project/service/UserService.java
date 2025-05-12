package com.example.senior_project.service;

import com.example.senior_project.dto.UserProfileUpdateRequest;
import com.example.senior_project.model.User;
import com.example.senior_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public User updateProfile(Long userId, UserProfileUpdateRequest request, User currentUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Kullanıcı sadece kendi profilini güncelleyebilir
        if (!user.getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only update your own profile");
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setBio(request.getBio());

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Follower sayısını hesapla
        int followerCount = user.getFollowers() != null ? user.getFollowers().size() : 0;
        user.setFollowerCount(followerCount);

        return user;
    }
}