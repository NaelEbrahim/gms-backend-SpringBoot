package com.graduation.GMS.DTO.Request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MessageRequest {

    private String socket_id;

    private String channel_name;

    private String senderId;

    private String receiverId;

    private String content;

    private List<Integer> messageIds;

    private Integer conversationId;

}
