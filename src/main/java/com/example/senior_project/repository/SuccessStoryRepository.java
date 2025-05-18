package com.example.senior_project.repository;

import com.example.senior_project.entity.SuccessStory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SuccessStoryRepository extends JpaRepository<SuccessStory, Long> {
    Page<SuccessStory> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<SuccessStory> findByApprovedTrue(Pageable pageable);

    List<SuccessStory> findByCategoryAndApprovedTrue(String category);

    List<SuccessStory> findByTitleContainingOrContentContainingAndApprovedTrue(String title, String content);
}