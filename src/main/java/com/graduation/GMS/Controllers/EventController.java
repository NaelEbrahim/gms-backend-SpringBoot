package com.graduation.GMS.Controllers;

import com.graduation.GMS.DTO.Request.CreateEventRequest;
import com.graduation.GMS.DTO.Request.EventRequest;
import com.graduation.GMS.DTO.Request.ImageRequest;
import com.graduation.GMS.DTO.Request.UpdateScoreRequest;
import com.graduation.GMS.Services.EventService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/event")
@AllArgsConstructor
public class EventController {
    private EventService eventService;

    @PostMapping("/create")
    public ResponseEntity<?> createEvent(@Valid @ModelAttribute CreateEventRequest request) {
        return eventService.createEvent(request);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable Integer id,
                                           @Valid @RequestBody EventRequest request) {
        return eventService.updateEvent(id, request);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable Integer id) {
        return eventService.deleteEvent(id);
    }

    @PutMapping("upload-image")
    public ResponseEntity<?> uploadEventImage(@ModelAttribute ImageRequest request){
        return eventService.uploadEventImage(request);
    }

    @GetMapping("/show/{id}")
    public ResponseEntity<?> getEventById(@PathVariable Integer id) {
        return eventService.getEventById(id);
    }

    @GetMapping("/show/all")
    public ResponseEntity<?> getAllEvents() {
        return eventService.getAllEvents();
    }

    @PostMapping("/subscribe/{id}")
    public ResponseEntity<?> subscribeToEvent(@Valid @PathVariable Integer id) {
        return eventService.subscribeToEvent(id);
    }
    @PostMapping("/unsubscribe/{id}")
    public ResponseEntity<?> unsubscribeFromEvent(@Valid @PathVariable Integer id) {
        return eventService.unsubscribeFromEvent(id);
    }

    // Score Management Endpoints
    @PostMapping("/scores")
    public ResponseEntity<?> updateUserScore(@Valid @RequestBody UpdateScoreRequest request) {
        return eventService.updateUserScore(request);
    }

    @GetMapping("/{eventId}/scores/my-score")
    public ResponseEntity<?> getUserScore(@PathVariable Integer eventId) {
        return eventService.getUserScore(eventId);
    }

    @GetMapping("/{eventId}/participants")
    public ResponseEntity<?> getEventParticipants(@PathVariable Integer eventId ,@RequestParam String rank) {
        if (rank.equalsIgnoreCase("desc")) {
            return eventService.getEventParticipantsDesc(eventId);
        }
        else if (rank.equalsIgnoreCase("asc")) {
            return eventService.getEventParticipantsASC(eventId);
        }
        else {
            return eventService.getEventParticipants(eventId);
        }

    }

}
