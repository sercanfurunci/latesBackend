package com.example.senior_project.controller;

import com.example.senior_project.model.Notification;
import com.example.senior_project.model.User;
import com.example.senior_project.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationRepository notificationRepository;

    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(notificationRepository.findByUserOrderByCreatedAtDesc(user));
    }

    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(notificationRepository.findByUserAndIsReadFalse(user));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(@AuthenticationPrincipal User user) {
        long count = notificationRepository.findByUserAndIsReadFalse(user).size();
        return ResponseEntity.ok(count);
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal User user) {
        notificationRepository.findByUserAndIsReadFalse(user)
                .forEach(n -> n.setIsRead(true));
        notificationRepository.flush();
        return ResponseEntity.ok().build();
    }
}