package com.example.senior_project.service;

import com.example.senior_project.model.EducationalContent;
import com.example.senior_project.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EducationalContentService {
    EducationalContent createContent(EducationalContent content, User author);

    EducationalContent updateContent(Long contentId, EducationalContent updatedContent, User author);

    Page<EducationalContent> getAllPublishedContent(Pageable pageable);

    List<EducationalContent> getContentByCategory(String category);

    EducationalContent getContentById(Long contentId);

    void publishContent(Long contentId, User admin);

    void deleteContent(Long contentId, User author);

    List<EducationalContent> searchContent(String keyword);
}