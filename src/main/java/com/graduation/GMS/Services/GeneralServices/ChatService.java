package com.graduation.GMS.Services.GeneralServices;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graduation.GMS.DTO.Request.MessageRequest;
import com.graduation.GMS.DTO.Response.ConversationDTO;
import com.graduation.GMS.DTO.Response.MessageResponse;
import com.graduation.GMS.DTO.Response.UserResponse;
import com.graduation.GMS.Handlers.HandleCurrentUserSession;
import com.graduation.GMS.Models.Message;
import com.graduation.GMS.Models.User;
import com.graduation.GMS.Repositories.MessageRepository;
import com.graduation.GMS.Repositories.UserRepository;
import com.pusher.rest.Pusher;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ChatService {

    private final Pusher pusher;

    private final UserRepository userRepository;

    private final MessageRepository messageRepository;


    public ResponseEntity<?> userChatAuth(MessageRequest messageRequest) throws JsonProcessingException {
        Integer senderId = HandleCurrentUserSession.getCurrentUser().getId();
        if (!userIsInChannel(messageRequest.getChannel_name(), senderId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "you are not belong to this chat"));
        }
        String authJson = pusher.authenticate(messageRequest.getSocket_id(), messageRequest.getChannel_name());
        return ResponseEntity.ok().body(new ObjectMapper().readValue(authJson, Map.class));
    }

    @Transactional
    public ResponseEntity<?> sendMessage(MessageRequest chatMessage) {
        var sender = HandleCurrentUserSession.getCurrentUser();
        var receiver = userRepository.findById(Integer.parseInt(chatMessage.getReceiverId()));
        if (receiver.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "receiver not found"));
        }

        String channelName = "private-chat-" + Math.min(sender.getId(), receiver.get().getId()) + "-" + Math.max(sender.getId(), receiver.get().getId());

        Map<String, String> payload = new HashMap<>();
        payload.put("senderId", sender.getId().toString());
        payload.put("receiverId", receiver.get().getId().toString());
        payload.put("message", chatMessage.getContent());
        payload.put("timeStamp", LocalDateTime.now().toString());
        pusher.trigger(channelName, "new-message", payload);

        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver.get());
        message.setContent(chatMessage.getContent());
        message.setType(chatMessage.getType());
        message.setDate(LocalDateTime.now());
        messageRepository.save(message);

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "message sent"));
    }


    private boolean userIsInChannel(String channelName, Integer userId) {
        String prefix = "private-chat-";
        if (!channelName.startsWith(prefix)) return false;
        String[] parts = channelName.substring(prefix.length()).split("-");
        if (parts.length != 2) return false;
        return userId.equals(Integer.parseInt(parts[0])) || userId.equals(Integer.parseInt(parts[1]));
    }

    public List<ConversationDTO> getGroupedConversations() {
        // Convert current user to UserResponse once
        UserResponse currentUserResponse = UserResponse.mapToUserResponse(HandleCurrentUserSession.getCurrentUser());

        return messageRepository.findDistinctConversationPartnerIds(HandleCurrentUserSession.getCurrentUser().getId())
                .stream()
                .map(partnerId -> {
                    User partner = userRepository.findById(partnerId)
                            .orElseThrow(() -> new RuntimeException("User not found"));

                    List<Message> messages = messageRepository.findConversationBetweenUsers(HandleCurrentUserSession.getCurrentUser(), partner);

                    if (messages.isEmpty()) return null;

                    // Convert all messages to MessageResponse
                    List<MessageResponse> messageResponses = messages.stream()
                            .map(MessageResponse::mapToMessageResponse)
                            .collect(Collectors.toList());

                    return new ConversationDTO(
                            UserResponse.mapToUserResponse(partner),
                            messageResponses,
                            messageResponses.get(messageResponses.size() - 1)
                    );
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(
                        dto -> dto.getLastMessage().getDate(),
                        Comparator.reverseOrder()
                ))
                .collect(Collectors.toList());
    }

}
