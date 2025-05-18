package com.example.senior_project.controller;

import com.example.senior_project.model.EducationalContent;
import com.example.senior_project.model.User;
import com.example.senior_project.service.EducationalContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/educational-contents")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class EducationalContentController {
    private final EducationalContentService educationalContentService;

    @GetMapping
    public ResponseEntity<?> getAllContents(Pageable pageable) {
        try {
            log.info("Fetching all educational contents with pagination");
            Page<EducationalContent> contents = educationalContentService.getAllPublishedContent(pageable);
            return ResponseEntity.ok(contents);
        } catch (Exception e) {
            log.error("Error fetching educational contents: ", e);
            return ResponseEntity.internalServerError()
                    .body("İçerikler yüklenirken bir hata oluştu: " + e.getMessage());
        }
    }

    @GetMapping("/{contentId}")
    public ResponseEntity<?> getContentById(@PathVariable Long contentId) {
        try {
            log.info("Fetching educational content with id: {}", contentId);
            EducationalContent content = educationalContentService.getContentById(contentId);
            return ResponseEntity.ok(content);
        } catch (Exception e) {
            log.error("Error fetching educational content with id {}: ", contentId, e);
            return ResponseEntity.internalServerError().body("İçerik yüklenirken bir hata oluştu: " + e.getMessage());
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<?> getContentsByCategory(@PathVariable String category) {
        try {
            log.info("Fetching educational contents for category: {}", category);
            List<EducationalContent> contents = educationalContentService.getContentByCategory(category);
            return ResponseEntity.ok(contents);
        } catch (Exception e) {
            log.error("Error fetching educational contents for category {}: ", category, e);
            return ResponseEntity.internalServerError()
                    .body("İçerikler yüklenirken bir hata oluştu: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchContents(@RequestParam String keyword) {
        try {
            log.info("Searching educational contents with keyword: {}", keyword);
            List<EducationalContent> contents = educationalContentService.searchContent(keyword);
            return ResponseEntity.ok(contents);
        } catch (Exception e) {
            log.error("Error searching educational contents with keyword {}: ", keyword, e);
            return ResponseEntity.internalServerError().body("İçerikler aranırken bir hata oluştu: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createContent(
            @RequestBody EducationalContent content,
            @AuthenticationPrincipal User author) {
        try {
            log.info("Creating new educational content: {}", content.getTitle());
            EducationalContent createdContent = educationalContentService.createContent(content, author);
            return ResponseEntity.ok(createdContent);
        } catch (Exception e) {
            log.error("Error creating educational content: ", e);
            return ResponseEntity.internalServerError()
                    .body("İçerik oluşturulurken bir hata oluştu: " + e.getMessage());
        }
    }

    @PutMapping("/{contentId}")
    public ResponseEntity<?> updateContent(
            @PathVariable Long contentId,
            @RequestBody EducationalContent content,
            @AuthenticationPrincipal User author) {
        try {
            log.info("Updating educational content with id: {}", contentId);
            EducationalContent updatedContent = educationalContentService.updateContent(contentId, content, author);
            return ResponseEntity.ok(updatedContent);
        } catch (Exception e) {
            log.error("Error updating educational content with id {}: ", contentId, e);
            return ResponseEntity.internalServerError()
                    .body("İçerik güncellenirken bir hata oluştu: " + e.getMessage());
        }
    }

    @DeleteMapping("/{contentId}")
    public ResponseEntity<?> deleteContent(
            @PathVariable Long contentId,
            @AuthenticationPrincipal User author) {
        try {
            log.info("Deleting educational content with id: {}", contentId);
            educationalContentService.deleteContent(contentId, author);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting educational content with id {}: ", contentId, e);
            return ResponseEntity.internalServerError().body("İçerik silinirken bir hata oluştu: " + e.getMessage());
        }
    }

    @PostMapping("/{contentId}/publish")
    public ResponseEntity<?> publishContent(
            @PathVariable Long contentId,
            @AuthenticationPrincipal User admin) {
        try {
            log.info("Publishing educational content with id: {}", contentId);
            educationalContentService.publishContent(contentId, admin);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error publishing educational content with id {}: ", contentId, e);
            return ResponseEntity.internalServerError().body("İçerik yayınlanırken bir hata oluştu: " + e.getMessage());
        }
    }
}