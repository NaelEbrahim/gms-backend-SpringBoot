package com.graduation.GMS.Controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.graduation.GMS.DTO.Request.FileChatRequest;
import com.graduation.GMS.DTO.Request.MessageRequest;
import com.graduation.GMS.Services.GeneralServices.ChatService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@AllArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;


    @PostMapping("/auth")
    public ResponseEntity<?> authenticate(@RequestBody MessageRequest messageRequest) {
        try {
            return chatService.userChatAuth(messageRequest);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/sendMessage")
    public ResponseEntity<?> sendMessage(@RequestBody @Valid MessageRequest messageRequest) {
        return chatService.sendMessage(messageRequest);
    }

    @PostMapping("/sendFile")
    public ResponseEntity<?> sendFile(@ModelAttribute FileChatRequest fileChatRequest) {
        return chatService.sendFile(fileChatRequest);
    }

    @GetMapping("/conversations")
    public ResponseEntity<?> getConversations() {
        return chatService.getUserConversations();
    }

    @GetMapping("/messages")
    public ResponseEntity<?> getMessages(
            @RequestParam Integer conversationId,
            @RequestParam Integer page,
            @RequestParam Integer size
    ) {
        return chatService.getMessages(conversationId, page, size);
    }

    @DeleteMapping("/deleteMessage")
    public ResponseEntity<?> deleteMessage(@RequestBody @Valid MessageRequest messageRequest) throws AccessDeniedException {
        return chatService.deleteMessage(messageRequest);
    }

    @DeleteMapping("/deleteConversation")
    public ResponseEntity<?> deleteConversation(@RequestBody @Valid MessageRequest messageRequest) throws AccessDeniedException {
        return chatService.deleteConversation(messageRequest);
    }

    @PutMapping("/updateLastSeen")
    public ResponseEntity<?> updateConversationLastSeen(@RequestParam Integer conversationId){
        return chatService.updateLastSeen(conversationId);
    }

}
