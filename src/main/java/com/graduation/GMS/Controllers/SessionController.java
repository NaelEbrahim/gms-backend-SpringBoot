package com.graduation.GMS.Controllers;

import com.graduation.GMS.DTO.Request.*;
import com.graduation.GMS.Services.SessionService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/session")
@AllArgsConstructor
public class SessionController {

    private SessionService sessionService;

    @PostMapping("/create")
    public ResponseEntity<?> createSession(@Valid @RequestBody SessionRequest request) {
        return sessionService.createSession(request);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateSession(@PathVariable Integer id,
                                           @Valid @RequestBody SessionRequest request) {
        return sessionService.updateSession(id, request);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteSession(@PathVariable Integer id) {
        return sessionService.deleteSession(id);
    }

    @GetMapping("/show/{id}")
    public ResponseEntity<?> getSessionById(@PathVariable Integer id) {
        return sessionService.getSessionById(id);
    }

    @GetMapping("/show/all")
    public ResponseEntity<?> getAllSessions() {
        return sessionService.getAllSessions();
    }


    // Assign a session to a user
    @PostMapping("/assign")
    public ResponseEntity<?> assignSessionToUser(@RequestBody AssignSessionToUserRequest request) {
        return sessionService.assignSessionToUser(request);
    }

    // Unassign a session from a user
    @PostMapping("/unassign")
    public ResponseEntity<?> unAssignSessionFromUser(@RequestBody AssignSessionToUserRequest request) {
        return sessionService.unAssignSessionToUser(request);
    }

    // Rate a session
    @PostMapping("/rate")
    public ResponseEntity<?> rateSession(@RequestBody RateSessionRequest request) {
        return sessionService.rateSession(request);
    }

    // Submit feedback for a session
    @PostMapping("/feedback")
    public ResponseEntity<?> submitFeedback(@RequestBody FeedBackSessionRequest request) {
        return sessionService.submitFeedback(request);
    }

    // Get all feedbacks for a specific session
    @GetMapping("/{sessionId}/feedbacks")
    public ResponseEntity<?> getAllSessionFeedbacks(@PathVariable Integer sessionId) {
        return sessionService.getAllSessionFeedBacks(sessionId);
    }

    // Get all sessions assigned to the currently logged-in user
    @GetMapping("/my-sessions")
    public ResponseEntity<?> getMyAssignedSessions() {

        return sessionService.getMyAssignedSessions();
    }

    // Get all sessions assigned to a specific user (Admin/Coach only)
    @GetMapping("/assigned/{userId}")
    public ResponseEntity<?> getAssignedSessionsByUserId(@PathVariable Integer userId) {
        return sessionService.getAssignedSessionsByUserId(userId);
    }

    @PostMapping("/attendance")
    public ResponseEntity<?> createAttendanceFromQr(@Valid @RequestBody QrSessionAttendanceRequest request) {
        return sessionService.createAttendanceFromQr(request);
    }

    @GetMapping("/{userId}/attendance/{sessionId}")
    public ResponseEntity<?> getUserAttendanceById(@PathVariable Integer userId,@PathVariable Integer sessionId) {
        return sessionService.getUserAttendanceById(userId,sessionId);
    }


}
