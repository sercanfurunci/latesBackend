package com.example.senior_project.service;

import com.example.senior_project.entity.SuccessStory;
import com.example.senior_project.entity.StoryComment;
import com.example.senior_project.model.User;
import com.example.senior_project.model.UserType;
import com.example.senior_project.repository.SuccessStoryRepository;
import com.example.senior_project.repository.StoryCommentRepository;
import com.example.senior_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SuccessStoryService {
    private final SuccessStoryRepository successStoryRepository;
    private final StoryCommentRepository storyCommentRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public Page<SuccessStory> getAllStories(Pageable pageable) {
        return successStoryRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public SuccessStory getStoryById(Long id) {
        return successStoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Story not found"));
    }

    @Transactional
    public SuccessStory createStory(SuccessStory story, User author) {
        story.setAuthor(author);
        return successStoryRepository.save(story);
    }

    @Transactional
    public SuccessStory shareStory(SuccessStory story, User author) {
        if (author.getUserType() != UserType.SELLER) {
            throw new RuntimeException("Sadece satıcılar başarı hikayesi paylaşabilir");
        }
        story.setAuthor(author);
        story.setApproved(false);
        return successStoryRepository.save(story);
    }

    @Transactional
    public SuccessStory updateStory(Long storyId, SuccessStory updatedStory, User author) {
        if (author.getUserType() != UserType.SELLER) {
            throw new RuntimeException("Sadece satıcılar başarı hikayelerini düzenleyebilir");
        }
        SuccessStory existingStory = successStoryRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Hikaye bulunamadı"));
        if (!existingStory.getAuthor().equals(author)) {
            throw new RuntimeException("Bu hikayeyi düzenleme yetkiniz yok");
        }
        existingStory.setTitle(updatedStory.getTitle());
        existingStory.setContent(updatedStory.getContent());
        existingStory.setCategory(updatedStory.getCategory());
        existingStory.setImageUrl(updatedStory.getImageUrl());
        return successStoryRepository.save(existingStory);
    }

    @Transactional
    public void approveStory(Long storyId, User admin) {
        SuccessStory story = successStoryRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Hikaye bulunamadı"));
        story.setApproved(true);
        successStoryRepository.save(story);
        com.example.senior_project.model.User modelUser = userRepository.findByEmail(story.getAuthor().getEmail())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        notificationService.createSystemNotification(
                modelUser,
                "Hikayeniz onaylandı ve yayınlandı: " + story.getTitle());
    }

    @Transactional(readOnly = true)
    public Page<SuccessStory> getAllApprovedStories(Pageable pageable) {
        return successStoryRepository.findByIsApprovedTrue(pageable);
    }

    @Transactional(readOnly = true)
    public List<SuccessStory> getStoriesByCategory(String category) {
        return successStoryRepository.findByCategoryAndIsApprovedTrue(category);
    }

    @Transactional
    public StoryComment addComment(Long storyId, String content, User author) {
        SuccessStory story = getStoryById(storyId);
        StoryComment comment = StoryComment.builder()
                .content(content)
                .story(story)
                .author(author)
                .build();
        return storyCommentRepository.save(comment);
    }

    @Transactional
    public void supportStory(Long storyId, User user) {
        SuccessStory story = successStoryRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Hikaye bulunamadı"));
        if (!story.getSupporters().contains(user)) {
            story.getSupporters().add(user);
            story.setSupportCount(story.getSupportCount() + 1);
            successStoryRepository.save(story);
            if (!story.getAuthor().equals(user)) {
                com.example.senior_project.model.User modelUser = userRepository
                        .findByEmail(story.getAuthor().getEmail())
                        .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
                notificationService.createSystemNotification(
                        modelUser,
                        user.getFirstName() + " " + user.getLastName() + " hikayenizi destekledi");
            }
        }
    }

    @Transactional
    public void removeSupport(Long storyId, User user) {
        SuccessStory story = successStoryRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Hikaye bulunamadı"));
        if (story.getSupporters().contains(user)) {
            story.getSupporters().remove(user);
            story.setSupportCount(story.getSupportCount() - 1);
            successStoryRepository.save(story);
        }
    }

    public List<StoryComment> getCommentsByStoryId(Long storyId) {
        return storyCommentRepository.findByStoryIdOrderByCreatedAtDesc(storyId);
    }

    @Transactional
    public void deleteStory(Long storyId, User author) {
        if (author.getUserType() != UserType.SELLER) {
            throw new RuntimeException("Sadece satıcılar başarı hikayelerini silebilir");
        }
        SuccessStory story = successStoryRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Hikaye bulunamadı"));
        if (!story.getAuthor().equals(author)) {
            throw new RuntimeException("Bu hikayeyi silme yetkiniz yok");
        }
        successStoryRepository.delete(story);
    }

    @Transactional(readOnly = true)
    public List<SuccessStory> searchStories(String keyword) {
        return successStoryRepository.findByTitleContainingOrContentContainingAndIsApprovedTrue(keyword, keyword);
    }
}