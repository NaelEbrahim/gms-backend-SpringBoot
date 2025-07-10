package com.graduation.GMS.DTO.Request;

import com.graduation.GMS.Models.Enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileChatRequest {
    private String socket_id;

    private String channel_name;

    private String senderId;

    private String receiverId;

    private MessageType messageType;

    private MultipartFile content;
}
