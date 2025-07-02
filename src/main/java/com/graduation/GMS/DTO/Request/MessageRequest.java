package com.graduation.GMS.DTO.Request;

import com.graduation.GMS.Models.Enums.MessageType;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
public class MessageRequest {

    private String socket_id;

    private String channel_name;

    private String senderId;

    private String receiverId;

    private String content;

    private MessageType type;

    private Instant timeStamp;

}
