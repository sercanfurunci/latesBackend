package com.example.senior_project.controller;

import com.example.senior_project.dto.MessageRequest;
import com.example.senior_project.model.Message;
import com.example.senior_project.model.User;
import com.example.senior_project.service.buyer.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<Message> sendMessage(
            @Valid @RequestBody MessageRequest request,
            @AuthenticationPrincipal User sender) {
        return ResponseEntity.ok(messageService.sendMessage(request, sender));
    }

    @GetMapping("/conversation/{otherUserId}")
    public ResponseEntity<List<Message>> getConversation(
            @PathVariable Long otherUserId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(messageService.getConversation(otherUserId, currentUser));
    }

    @PutMapping("/{messageId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long messageId,
            @AuthenticationPrincipal User currentUser) {
        messageService.markAsRead(messageId, currentUser);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<Void> markAllMessagesAsRead(@AuthenticationPrincipal User currentUser) {
        messageService.markAllMessagesAsRead(currentUser.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{messageId}/mark-read")
    public ResponseEntity<Void> markMessageAsRead(
            @PathVariable Long messageId,
            @AuthenticationPrincipal User currentUser) {
        messageService.markMessageAsRead(messageId, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{messageId}/unmark-read")
    public ResponseEntity<Void> unmarkMessageAsRead(
            @PathVariable Long messageId,
            @AuthenticationPrincipal User currentUser) {
        messageService.unmarkMessageAsRead(messageId, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unmark-all-read")
    public ResponseEntity<Void> unmarkAllMessagesAsRead(@AuthenticationPrincipal User currentUser) {
        messageService.unmarkAllMessagesAsRead(currentUser.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<Message>> getAllMessages(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(messageService.getAllMessages(currentUser));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadMessagesCount(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(messageService.getUnreadMessagesCount(currentUser));
    }
}