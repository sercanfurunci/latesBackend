package com.example.senior_project.controller;

import com.example.senior_project.model.ChatMessage;
import com.example.senior_project.model.User;
import com.example.senior_project.model.ChatBotFeedback;
import com.example.senior_project.service.ChatBotService;
import com.example.senior_project.repository.ChatBotFeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/chatbot")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ChatBotController {

    private final ChatBotService chatBotService;
    private final ChatBotFeedbackRepository feedbackRepository;

    @Value("${chatbot.mode:AI}")
    private String chatbotMode;

    @PostMapping("/message")
    public ResponseEntity<ChatMessage> handleMessage(
            @RequestBody ChatMessage message,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            ChatMessage errorResponse = new ChatMessage();
            errorResponse.setText("Lütfen giriş yapın.");
            errorResponse.setSender("bot");
            errorResponse.setTimestamp(java.time.LocalDateTime.now());
            errorResponse.setMessageType("text");
            return ResponseEntity.ok(errorResponse);
        }

        ChatMessage response = chatBotService.processMessage(message, (User) userDetails);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/feedback")
    public ResponseEntity<ChatBotFeedback> submitFeedback(
            @RequestBody ChatBotFeedback feedback,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.badRequest().build();
        }

        feedback.setUser((User) userDetails);
        ChatBotFeedback savedFeedback = feedbackRepository.save(feedback);
        return ResponseEntity.ok(savedFeedback);
    }

    @GetMapping("/feedback")
    public ResponseEntity<List<ChatBotFeedback>> getFeedback(
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.badRequest().build();
        }

        List<ChatBotFeedback> feedback = feedbackRepository.findByUser((User) userDetails);
        return ResponseEntity.ok(feedback);
    }

    @PutMapping("/admin/mode")
    public ResponseEntity<Map<String, String>> setChatbotMode(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null || !((User) userDetails).getUserType().name().equals("ADMIN")) {
            return ResponseEntity.badRequest().build();
        }

        String mode = request.get("mode");
        if (mode != null && (mode.equals("AI") || mode.equals("RULE_BASED"))) {
            // Update the mode in application.properties or database
            // For now, we'll just return the new mode
            return ResponseEntity.ok(Map.of("mode", mode));
        }

        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/admin/mode")
    public ResponseEntity<Map<String, String>> getChatbotMode(
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null || !((User) userDetails).getUserType().name().equals("ADMIN")) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(Map.of("mode", chatbotMode));
    }
}