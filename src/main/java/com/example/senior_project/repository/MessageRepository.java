package com.example.senior_project.repository;

import com.example.senior_project.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m WHERE (m.sender.id = :user1Id AND m.receiver.id = :user2Id) OR (m.sender.id = :user2Id AND m.receiver.id = :user1Id) ORDER BY m.createdAt")
    List<Message> findConversation(Long user1Id, Long user2Id);

    List<Message> findAllBySenderIdOrReceiverId(Long senderId, Long receiverId);

    Long countByReceiverIdAndIsReadFalse(Long receiverId);

    List<Message> findAllByReceiverIdAndIsReadFalse(Long receiverId);

    List<Message> findAllByReceiverIdAndIsReadTrue(Long receiverId);
}