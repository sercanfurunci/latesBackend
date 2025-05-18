package com.example.senior_project.controller;

import com.example.senior_project.entity.SuccessStory;
import com.example.senior_project.entity.StoryComment;
import com.example.senior_project.model.User;
import com.example.senior_project.service.SuccessStoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/success-stories")
@RequiredArgsConstructor
public class SuccessStoryController {
    private final SuccessStoryService successStoryService;

    @GetMapping
    public ResponseEntity<Page<SuccessStory>> getAllStories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(successStoryService.getAllStories(PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessStory> getStoryById(@PathVariable Long id) {
        return ResponseEntity.ok(successStoryService.getStoryById(id));
    }

    @PostMapping
    public ResponseEntity<SuccessStory> createStory(
            @RequestBody SuccessStory story,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(successStoryService.createStory(story, user));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<StoryComment> addComment(
            @PathVariable Long id,
            @RequestBody String content,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(successStoryService.addComment(id, content, user));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<StoryComment>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(successStoryService.getCommentsByStoryId(id));
    }
}