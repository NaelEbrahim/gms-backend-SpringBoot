package com.graduation.GMS.Services;

import com.graduation.GMS.DTO.Request.EventRequest;
import com.graduation.GMS.DTO.Request.UpdateScoreRequest;
import com.graduation.GMS.DTO.Response.*;
import com.graduation.GMS.Models.Event;
import com.graduation.GMS.Models.Event_Participant;
import com.graduation.GMS.Models.User;
import com.graduation.GMS.Repositories.EventRepository;
import com.graduation.GMS.Repositories.Event_ParticipantRepository;
import com.graduation.GMS.Repositories.PrizeRepository;
import com.graduation.GMS.Repositories.UserRepository;
import com.graduation.GMS.Tools.HandleCurrentUserSession;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.graduation.GMS.DTO.Response.UserResponse.mapToUserResponse;

@Service
@AllArgsConstructor
public class EventService {

    private EventRepository eventRepository;

    private PrizeRepository prizeRepository;

    private Event_ParticipantRepository eventParticipantRepository;

    private UserRepository userRepository;

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Secretary')")
    public ResponseEntity<?> createEvent(EventRequest request) {

        // Check if the event title already exists (optional validation)
        Optional<Event> existingEvent = eventRepository.findByTitle(request.getTitle());
        if (existingEvent.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Event title already exists"));
        }
        // Convert the DTO to entity and save
        Event eventEntity = new Event();

        eventEntity.setAdmin(HandleCurrentUserSession.getCurrentUser());
        eventEntity.setTitle(request.getTitle());
        eventEntity.setDescription(request.getDescription());
        eventEntity.setStartedAt(request.getStartedAt());
        // Save the event to the database
        eventRepository.save(eventEntity);
        // Return the response with the saved event details
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Event created successfully"));

    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Secretary')")
    public ResponseEntity<?> updateEvent(Integer id, EventRequest request) {
        Optional<Event> optionalEvent = eventRepository.findById(id);
        if (optionalEvent.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Event not found"));
        }

        Event existingEvent = optionalEvent.get();

        if (!existingEvent.getTitle().equals(request.getTitle())&&!request.getTitle().isEmpty()) {
            existingEvent.setTitle(request.getTitle());
        }
        if (!existingEvent.getDescription().equals(request.getDescription())&&!request.getDescription().isEmpty()) {
            existingEvent.setDescription(request.getDescription());
        }
        if (!existingEvent.getStartedAt().equals(request.getStartedAt())&&request.getStartedAt()!=null) {
            existingEvent.setStartedAt(request.getStartedAt());
        }
        eventRepository.save(existingEvent);

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Event updated successfully"));
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Secretary')")
    public ResponseEntity<?> deleteEvent(Integer id) {
        if (!eventRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Event not found"));
        }

        eventRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Event deleted successfully"));
    }

    public ResponseEntity<?> getEventById(Integer id) {
        Optional<Event> eventOptional = eventRepository.findById(id);
        if (eventOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Event not found"));
        }

        Event event = eventOptional.get();
        return ResponseEntity.ok(buildEventResponse(event));
    }

    public ResponseEntity<?> getAllEvents() {
        List<Event> events = eventRepository.findAll();
        if (events.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No events found"));
        }

        List<EventResponse> responses = events.stream()
                .map(this::buildEventResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    private EventResponse buildEventResponse(Event event) {
        UserResponse adminResponse = mapToUserResponse(event.getAdmin());

        // Get prizes directly from the PrizeRepository since it's one-to-many
        List<PrizeResponse> prizeResponses = prizeRepository.findByEvent(event).stream()
                .map(prize -> new PrizeResponse(
                        prize.getId(),
                        prize.getDescription(),
                        prize.getPrecondition()
                ))
                .collect(Collectors.toList());

        return new EventResponse(
                event.getId(),
                adminResponse,
                event.getTitle(),
                event.getDescription(),
                event.getStartedAt(),
                prizeResponses
        );
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('User')")
    public ResponseEntity<?> subscribeToEvent(Integer id) {
        User currentUser = HandleCurrentUserSession.getCurrentUser();
        Optional<Event> eventOptional = eventRepository.findById(id);

        if (eventOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Event not found"));
        }

        Event event = eventOptional.get();

        // Check if subscription is within 1 day of event start
        if (isWithinOneDayBeforeStart(event.getStartedAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Cannot subscribe within 1 day of event start"));
        }

        if (eventParticipantRepository.existsByUserAndEvent(currentUser, event)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "You are already subscribed to this event"));
        }

        Event_Participant participation = new Event_Participant();
        participation.setUser(currentUser);
        participation.setEvent(event);
        participation.setScore(0.0f);

        eventParticipantRepository.save(participation);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Successfully subscribed to the event"));
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('User')")
    public ResponseEntity<?> unsubscribeFromEvent(Integer id) {
        User currentUser = HandleCurrentUserSession.getCurrentUser();
        Optional<Event> eventOptional = eventRepository.findById(id);

        if (eventOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Event not found"));
        }

        Event event = eventOptional.get();

        // Check if unsubscription is within 1 day of event start
        if (isWithinOneDayBeforeStart(event.getStartedAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Cannot unsubscribe within 1 day of event start"));
        }

        Optional<Event_Participant> participation = eventParticipantRepository.findByUserAndEvent(currentUser, event);

        if (participation.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "You are not subscribed to this event"));
        }

        eventParticipantRepository.delete(participation.get());

        return ResponseEntity.ok(Map.of("message", "Successfully unsubscribed from the event"));
    }

    // Helper method to check if current time is within 1 day before event start
    private boolean isWithinOneDayBeforeStart(LocalDateTime eventStart) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneDayBefore = eventStart.minusDays(1);
        return now.isAfter(oneDayBefore) && now.isBefore(eventStart);
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Secretary')") // Only authorized roles can update scores
    public ResponseEntity<?> updateUserScore(UpdateScoreRequest request) {

        Optional<User> user = userRepository.findById(request.getUserId());
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User Not Found"));
        }
        Optional<Event> event = eventRepository.findById(request.getEventId());
        if (event.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Event Not Found"));
        }

        // Check participation
        Optional<Event_Participant> participation =
                eventParticipantRepository.findByUserAndEvent(user.get(), event.get());
        if (participation.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Participation record not found"));
        }

        // 4. Update the score
        participation.get().setScore(request.getScore());
        eventParticipantRepository.save(participation.get());

        // 5. Return success response
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Score updated successfully For The User with ID: " + request.getUserId()
                                                                            +" And Event ID: " + request.getEventId()));
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('User')")
    public ResponseEntity<?> getUserScore(Integer eventId) {
        // Get current user
        User currentUser = HandleCurrentUserSession.getCurrentUser();

        // Find the event
        Optional<Event> eventOptional = eventRepository.findById(eventId);
        if (eventOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Event not found"));
        }
        Event event = eventOptional.get();

        // Check participation
        Optional<Event_Participant> participation =
                eventParticipantRepository.findByUserAndEvent(currentUser, event);
        if (participation.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Participation record not found"));
        }

        // Check if one day has passed since event started
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneDayAfterEvent = event.getStartedAt().plusDays(1);

        if (now.isBefore(oneDayAfterEvent)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "message", "Scores will be available one day after the event",
                            "available_at", oneDayAfterEvent.toString()
                    ));
        }

        // Return the score if all conditions are met
        return ResponseEntity.ok(Map.of(
                "score", participation.get().getScore(),
                "event_title", event.getTitle(),
                "event_started_at", event.getStartedAt().toString()
        ));
    }


    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Secretary')")
    public ResponseEntity<?> getEventParticipants(Integer eventId) {
        // Find the event
        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Event not found"));
        }


        // Get all participants with their scores
        List<ParticipantResponse> participants = eventParticipantRepository.findByEvent(event.get())
                .stream()
                .map(participation -> new ParticipantResponse(
                        participation.getUser(),
                        participation.getScore()
                ))
                .collect(Collectors.toList());

        if (participants.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        // Build response
        EventParticipantResponse response = new EventParticipantResponse();
        response.setTitle(event.get().getTitle());
        response.setStartedAt(event.get().getStartedAt());
        response.setParticipants(participants);

        return ResponseEntity.ok(response);
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Secretary')")
    public ResponseEntity<?> getEventParticipantsASC(Integer eventId) {
        // Find the event
        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Event not found"));
        }


        // Get all participants with their scores
        List<ParticipantResponse> participants = eventParticipantRepository.findByEventOrderByScoreAsc(event.get())
                .stream()
                .map(participation -> new ParticipantResponse(
                        participation.getUser(),
                        participation.getScore()
                ))
                .collect(Collectors.toList());

        if (participants.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        // Build response
        EventParticipantResponse response = new EventParticipantResponse();
        response.setTitle(event.get().getTitle());
        response.setStartedAt(event.get().getStartedAt());
        response.setParticipants(participants);

        return ResponseEntity.ok(response);
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Secretary')")
    public ResponseEntity<?> getEventParticipantsDesc(Integer eventId) {
        // Find the event
        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Event not found"));
        }


        // Get all participants with their scores
        List<ParticipantResponse> participants = eventParticipantRepository.findByEventOrderByScoreDesc(event.get())
                .stream()
                .map(participation -> new ParticipantResponse(
                        participation.getUser(),
                        participation.getScore()
                ))
                .collect(Collectors.toList());

        if (participants.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        // Build response
        EventParticipantResponse response = new EventParticipantResponse();
        response.setTitle(event.get().getTitle());
        response.setStartedAt(event.get().getStartedAt());
        response.setParticipants(participants);

        return ResponseEntity.ok(response);
    }

}
