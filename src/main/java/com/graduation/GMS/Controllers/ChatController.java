package com.graduation.GMS.Controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.graduation.GMS.DTO.Request.MessageRequest;
import com.graduation.GMS.Services.GeneralServices.ChatService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/chat")
@CrossOrigin("http://localhost:63342")
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

    @GetMapping("/conversations")
    public ResponseEntity<?> getConversations() {
        return ResponseEntity.ok(chatService.getGroupedConversations());
    }

//    @GetMapping("/getMessages")
//    public ResponseEntity<?> getMessages(@RequestParam String userId, @RequestParam String coachId) {
//        try {
//            List<Message> messages = chatService.getMessages(userId, coachId);
//            return ResponseEntity.ok(messages);
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body("Error: " + e.getMessage());
//        }
//    }


}
