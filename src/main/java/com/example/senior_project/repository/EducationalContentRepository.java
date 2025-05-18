package com.example.senior_project.repository;

import com.example.senior_project.model.EducationalContent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EducationalContentRepository extends JpaRepository<EducationalContent, Long> {
    Page<EducationalContent> findByIsPublishedTrue(Pageable pageable);

    List<EducationalContent> findByCategoryAndIsPublishedTrue(String category);

    List<EducationalContent> findByTitleContainingOrDescriptionContainingAndIsPublishedTrue(String titleKeyword,
            String descriptionKeyword);

    List<EducationalContent> findByCategory(String category);
}