package com.graduation.GMS.DTO.Response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.graduation.GMS.Models.Message;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    private Integer id;

    private String content;

    private String type;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime date;

    private Integer conversationId;

    private UserResponse sender;

    private UserResponse receiver;


    public static MessageResponse mapToMessageResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getContent(),
                message.getType() != null ? message.getType().name() : null,
                message.getDate(),
                message.getConversation().getId(),
                UserResponse.mapToUserResponse(message.getSender()),
                UserResponse.mapToUserResponse(message.getReceiver())
        );
    }
}