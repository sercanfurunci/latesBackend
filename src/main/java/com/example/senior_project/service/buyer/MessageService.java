package com.example.senior_project.service.buyer;

import com.example.senior_project.dto.MessageRequest;
import com.example.senior_project.model.Message;
import com.example.senior_project.model.Offer;
import com.example.senior_project.model.User;
import com.example.senior_project.repository.MessageRepository;
import com.example.senior_project.repository.OfferRepository;
import com.example.senior_project.repository.UserRepository;
import com.example.senior_project.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final OfferRepository offerRepository;
    private final NotificationService notificationService;

    public Message sendMessage(MessageRequest request, User sender) {
        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        Offer relatedOffer = null;
        if (request.getOfferId() != null) {
            relatedOffer = offerRepository.findById(request.getOfferId())
                    .orElseThrow(() -> new RuntimeException("Offer not found"));
        }

        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(request.getContent())
                .relatedOffer(relatedOffer)
                .isRead(false)
                .build();

        Message savedMessage = messageRepository.save(message);
        notificationService.notifyNewMessage(receiver, "New message received");

        return savedMessage;
    }

    public List<Message> getConversation(Long otherUserId, User currentUser) {
        return messageRepository.findConversation(currentUser.getId(), otherUserId);
    }

    public void markAsRead(Long messageId, User currentUser) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (!message.getReceiver().equals(currentUser)) {
            throw new RuntimeException("Not authorized to mark this message as read");
        }

        message.setRead(true);
        messageRepository.save(message);
    }

    public List<Message> getAllMessages(User currentUser) {
        return messageRepository.findAllBySenderIdOrReceiverId(currentUser.getId(), currentUser.getId());
    }

    public Long getUnreadMessagesCount(User currentUser) {
        return messageRepository.countByReceiverIdAndIsReadFalse(currentUser.getId());
    }

    public void markAllMessagesAsRead(Long userId) {
        List<Message> unreadMessages = messageRepository.findAllByReceiverIdAndIsReadFalse(userId);
        for (Message message : unreadMessages) {
            message.setRead(true);
        }
        messageRepository.saveAll(unreadMessages);
    }

    public void markMessageAsRead(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        if (!message.getReceiver().getId().equals(userId)) {
            throw new RuntimeException("Not authorized to mark this message as read");
        }
        message.setRead(true);
        messageRepository.save(message);
    }

    public void unmarkMessageAsRead(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        if (!message.getReceiver().getId().equals(userId)) {
            throw new RuntimeException("Not authorized to unmark this message as read");
        }
        message.setRead(false);
        messageRepository.save(message);
    }

    public void unmarkAllMessagesAsRead(Long userId) {
        List<Message> readMessages = messageRepository.findAllByReceiverIdAndIsReadTrue(userId);
        for (Message message : readMessages) {
            message.setRead(false);
        }
        messageRepository.saveAll(readMessages);
    }
}