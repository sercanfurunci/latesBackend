package com.example.senior_project.repository;

import com.example.senior_project.entity.StoryComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoryCommentRepository extends JpaRepository<StoryComment, Long> {
    List<StoryComment> findByStoryIdOrderByCreatedAtDesc(Long storyId);
}