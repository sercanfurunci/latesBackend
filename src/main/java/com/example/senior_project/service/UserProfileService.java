package com.example.senior_project.service;

import com.example.senior_project.model.User;
import com.example.senior_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final UserRepository userRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Transactional
    public User uploadProfilePicture(Long userId, MultipartFile file, User currentUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Yetki kontrolü
        if (!currentUser.getId().equals(userId) && !currentUser.getUserType().equals("ADMIN")) {
            throw new AccessDeniedException("You don't have permission to update this profile");
        }

        try {
            // Profil fotoğrafları için src/profiles klasörünü kullan
            String profileDir = "src/profiles";
            File directory = new File(profileDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Dosya adını oluştur (userId ile benzersiz yap)
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = "profile_" + userId + "_" + UUID.randomUUID().toString() + extension;

            // Dosyayı kaydet
            Path filePath = Paths.get(profileDir, filename);
            Files.copy(file.getInputStream(), filePath);

            // Eski profil fotoğrafını sil
            if (user.getProfilePicture() != null) {
                Path oldFilePath = Paths.get(profileDir, user.getProfilePicture());
                Files.deleteIfExists(oldFilePath);
            }

            // Kullanıcı bilgilerini güncelle
            user.setProfilePicture(filename);
            return userRepository.save(user);

        } catch (IOException e) {
            log.error("Error uploading profile picture: ", e);
            throw new RuntimeException("Failed to upload profile picture");
        }
    }

    public String getProfilePicturePath(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getProfilePicture();
    }
}