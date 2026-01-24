package com.graduation.GMS.DTO.Response;

import com.graduation.GMS.Models.Enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
public class ConversationResponse {

    private Integer conversationId;

    private Integer otherUserId;

    private String otherUsername;

    private String otherUserProfileImage;

    private String lastMessage;

    private MessageType lastMessageType;

    private LocalDateTime lastMessageTime;

    private Integer unreadCount;

}