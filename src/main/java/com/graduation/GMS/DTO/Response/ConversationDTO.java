package com.graduation.GMS.DTO.Response;


import com.graduation.GMS.Models.Message;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDTO {
    private UserResponse otherParticipant;
    private List<MessageResponse> messages;
    private MessageResponse lastMessage;
}
