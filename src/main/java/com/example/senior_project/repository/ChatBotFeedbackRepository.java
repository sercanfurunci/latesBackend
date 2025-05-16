package com.example.senior_project.repository;

import com.example.senior_project.model.ChatBotFeedback;
import com.example.senior_project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatBotFeedbackRepository extends JpaRepository<ChatBotFeedback, Long> {
    List<ChatBotFeedback> findByUser(User user);

    List<ChatBotFeedback> findByIsHelpful(boolean isHelpful);
}