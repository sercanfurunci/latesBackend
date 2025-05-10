package com.example.senior_project.controller;

import com.example.senior_project.model.User;
import com.example.senior_project.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserProfileController {
    private final UserProfileService userProfileService;

    @PostMapping("/{userId}/profile-picture")
    public ResponseEntity<User> uploadProfilePicture(
            @PathVariable Long userId,
            @RequestParam("profilePicture") MultipartFile file,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(userProfileService.uploadProfilePicture(userId, file, currentUser));
    }

    @GetMapping("/{userId}/profile-picture")
    public ResponseEntity<String> getProfilePicture(@PathVariable Long userId) {
        return ResponseEntity.ok(userProfileService.getProfilePicturePath(userId));
    }
}