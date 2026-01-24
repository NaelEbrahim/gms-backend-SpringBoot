package com.graduation.GMS.Services.GeneralServices;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graduation.GMS.DTO.Request.FileChatRequest;
import com.graduation.GMS.DTO.Request.MessageRequest;
import com.graduation.GMS.DTO.Response.ConversationResponse;
import com.graduation.GMS.DTO.Response.MessageResponse;
import com.graduation.GMS.Handlers.HandleCurrentUserSession;
import com.graduation.GMS.Models.Conversation;
import com.graduation.GMS.Models.Enums.MessageType;
import com.graduation.GMS.Models.Message;
import com.graduation.GMS.Models.User;
import com.graduation.GMS.Models.User_Conversation;
import com.graduation.GMS.Repositories.*;
import com.graduation.GMS.Tools.FilesManagement;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.*;

@Service
@AllArgsConstructor
public class ChatService {

    private final PusherService pusherService;

    private final UserRepository userRepository;

    private final MessageRepository messageRepository;

    private final ConversationRepository conversationRepository;

    private final UserConversationRepository userConversationRepository;


    public ResponseEntity<?> userChatAuth(MessageRequest messageRequest) throws JsonProcessingException {
        Integer senderId = HandleCurrentUserSession.getCurrentUser().getId();
        String authJson = pusherService.getPusher().authenticate(messageRequest.getSocket_id(), messageRequest.getChannel_name());
        return ResponseEntity.ok().body(new ObjectMapper().readValue(authJson, Map.class));
    }

    @Transactional
        public ResponseEntity<?> sendMessage(MessageRequest chatMessage) {
        User sender = HandleCurrentUserSession.getCurrentUser();
        User receiver = userRepository.findById(Integer.parseInt(chatMessage.getReceiverId()))
                .orElseThrow(() -> new EntityNotFoundException("receiver not found"));

        String channelName = "private-user-" + receiver.getId();

        Conversation conversation = getOrCreateConversation(sender, receiver);

        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setConversation(conversation);
        message.setIsRead(false);
        message.setContent(chatMessage.getContent());
        message.setType(MessageType.TEXT);
        messageRepository.save(message);

        Map<String, String> payload = new HashMap<>();
        payload.put("conversationId" , conversation.getId().toString());
        payload.put("senderId", sender.getId().toString());
        payload.put("senderFirstName", sender.getFirstName());
        payload.put("senderLastName", sender.getLastName());
        payload.put("senderProfileImage",sender.getProfileImagePath());
        payload.put("receiverId", receiver.getId().toString());
        payload.put("messageId", message.getId().toString());
        payload.put("messageType", message.getType().toString());
        payload.put("message", chatMessage.getContent());
        payload.put("timeStamp", LocalDateTime.now().toString());
        pusherService.sendPusherEvent(channelName, "new-message", payload);

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", MessageResponse.mapToMessageResponse(message)));
    }

    @Transactional
    public ResponseEntity<?> sendFile(FileChatRequest chatFile) {
        User sender = HandleCurrentUserSession.getCurrentUser();
        User receiver = userRepository.findById(Integer.parseInt(chatFile.getReceiverId()))
                .orElseThrow(() -> new EntityNotFoundException("receiver not found"));

        String channelName = "private-user-" + receiver.getId();

        String filePath = null;
        if (chatFile.getMessageType().equals(MessageType.IMAGE)) {
            filePath = FilesManagement.uploadChatFile(chatFile.getContent(), sender.getId(),
                    receiver.getId(), "images");
        } else if (chatFile.getMessageType().equals(MessageType.VIDEO)) {
            filePath = FilesManagement.uploadChatFile(chatFile.getContent(), sender.getId(),
                    receiver.getId(), "videos");
        } else if (chatFile.getMessageType().equals(MessageType.AUDIO)) {
            filePath = FilesManagement.uploadChatFile(chatFile.getContent(), sender.getId(),
                    receiver.getId(), "audios");
        } else if (chatFile.getMessageType().equals(MessageType.DOCUMENT)) {
            filePath = FilesManagement.uploadChatFile(chatFile.getContent(), sender.getId(),
                    receiver.getId(), "documents");
        }

        if (filePath == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Upload failed"));
        }

        Conversation conversation = getOrCreateConversation(sender, receiver);

        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setConversation(conversation);
        message.setIsRead(false);
        message.setContent(filePath);
        message.setType(chatFile.getMessageType());
        messageRepository.save(message);

        Map<String, String> payload = new HashMap<>();
        payload.put("conversationId" , conversation.getId().toString());
        payload.put("senderId", sender.getId().toString());
        payload.put("senderFirstName", sender.getFirstName());
        payload.put("senderLastName", sender.getLastName());
        payload.put("senderProfileImage",sender.getProfileImagePath());
        payload.put("receiverId", receiver.getId().toString());
        payload.put("messageId", message.getId().toString());
        payload.put("messageType", message.getType().toString());
        payload.put("message", filePath);
        payload.put("timeStamp", LocalDateTime.now().toString());
        pusherService.sendPusherEvent(channelName, "new-message", payload);

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", MessageResponse.mapToMessageResponse(message)));
    }

    @Transactional
    private Conversation getOrCreateConversation(User userA, User userB) {
        long key = generateConversationKey(userA.getId(), userB.getId());
        Conversation conversation = conversationRepository.findByConversationKey(key).orElse(null);
        if (conversation != null) {
            // Conversation exists -> reset deletion for both users
            resetDeletionIfNeeded(userA, conversation);
            resetDeletionIfNeeded(userB, conversation);
            return conversation;
        }
        // Conversation not exists -> Create new conversation
        Conversation c = new Conversation();
        c.setConversationKey(key);
        Conversation saved = conversationRepository.save(c);

        User_Conversation ucA = new User_Conversation();
        ucA.setUser(userA);
        ucA.setConversation(saved);
        userConversationRepository.save(ucA);

        User_Conversation ucB = new User_Conversation();
        ucB.setUser(userB);
        ucB.setConversation(saved);
        userConversationRepository.save(ucB);

        return saved;
    }

    private void resetDeletionIfNeeded(User user, Conversation conversation) {
        User_Conversation uc = userConversationRepository.findByUserIdAndConversationId(user.getId(), conversation.getId())
                        .orElse(null);
        if (uc != null && uc.getIsDeleted() == true) {
            uc.setIsDeleted(false);
            userConversationRepository.save(uc);
        }
    }

    private long generateConversationKey(int userA, int userB) {
        int min = Math.min(userA, userB);
        int max = Math.max(userA, userB);
        return ((long) min << 32) | (max & 0xffffffffL);
    }

    public ResponseEntity<?> getUserConversations() {
        Integer userId = HandleCurrentUserSession.getCurrentUser().getId();
        List<User_Conversation> userConversations = userConversationRepository.findVisibleForUser(userId);
        List<ConversationResponse> summaries = new ArrayList<>();
        for (User_Conversation uc : userConversations) {
            Conversation conversation = uc.getConversation();
            User other = null;
            // not complicated (just loop 2 items)
            for (User_Conversation participant : conversation.getConversationUsers()) {
                if (!participant.getUser().getId().equals(userId)) {
                    other = participant.getUser();
                    break;
                }
            }
            if (other != null) {
                Message last = messageRepository.findLastMessage(conversation.getId());
                summaries.add(new ConversationResponse(
                        conversation.getId(),
                        other.getId(),
                        other.getFirstName() + " " + other.getLastName(),
                        other.getProfileImagePath(),
                        last != null ? last.getContent() : null,
                        last != null ? last.getType() : null,
                        last != null ? last.getDate() : null,
                        getUnreadCount(uc)
                ));
            }
        }
        return ResponseEntity.ok(Map.of("message", summaries));
    }

    private Integer getUnreadCount(User_Conversation uc) {
        LocalDateTime after = null;
        if (uc.getDeletedAt() != null) {
            after = uc.getDeletedAt();
        }
        if (uc.getLastSeenAt() != null) {
            if (after == null || uc.getLastSeenAt().isAfter(after)) {
                after = uc.getLastSeenAt();
            }
        }
        if (after == null) {
            after = LocalDateTime.of(1000, 1, 1, 0, 0);
        }
        return messageRepository.countUnreadForConversation(
                uc.getConversation().getId(),
                uc.getUser().getId(),
                after
        );
    }

    public ResponseEntity<?> getMessages(Integer conversationId, Integer page, Integer size) {
        User user = HandleCurrentUserSession.getCurrentUser();
        List<MessageResponse> chatMessages = new ArrayList<>();
        User_Conversation userConversation = userConversationRepository.findByUserIdAndConversationId(user.getId(),conversationId).orElse(null);
        if (userConversation != null){
            List<Message> messagesPage = messageRepository.findMessagesAfterDeletedAt(conversationId,userConversation.getDeletedAt(),PageRequest.of(page, size));
            for (Message item : messagesPage) {
                chatMessages.add(MessageResponse.mapToMessageResponse(item));
            }
            updateLastSeen(conversationId);
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", chatMessages));
    }

    public ResponseEntity<?> updateLastSeen (Integer conversationId){
        Integer userId = HandleCurrentUserSession.getCurrentUser().getId();
        userConversationRepository.updateLastSeenAt(userId, conversationId, LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "successfully"));
    }

    @Transactional
    public ResponseEntity<?> deleteMessage(MessageRequest messageRequest) throws AccessDeniedException {
        User user = HandleCurrentUserSession.getCurrentUser();
        List<Message> messages = messageRepository.findAllById(messageRequest.getMessageIds());
        if (!messages.isEmpty()) {
            for (Message message : messages) {
                if (!message.getSender().getId().equals(user.getId())) {
                    throw new AccessDeniedException("unAuthorized to delete this message");
                }
            }
            messageRepository.deleteAllById(messageRequest.getMessageIds());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(Map.of("message", "messages deleted"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "no messages found"));
    }

    @Transactional
    public ResponseEntity<?> deleteConversation(MessageRequest messageRequest) {
        Integer userId = HandleCurrentUserSession.getCurrentUser().getId();
        userConversationRepository.markDeletedForUser(userId, messageRequest.getConversationId(),LocalDateTime.now());
        // Check if both participants deleted → fully remove conversation
        List<User_Conversation> participants = userConversationRepository.findAllByConversationId(messageRequest.getConversationId());
        boolean allDeleted = true;
        for (User_Conversation uc : participants) {
            if (uc.getIsDeleted() == false) {
                allDeleted = false;
                break;
            }
        }
        if (allDeleted) {
            conversationRepository.deleteById(messageRequest.getConversationId());
            conversationRepository.flush();
        }
        return ResponseEntity.ok(Map.of("message", "Conversation deleted successfully"));
    }


}
