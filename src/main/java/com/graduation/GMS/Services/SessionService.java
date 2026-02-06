package com.graduation.GMS.Services;

import com.graduation.GMS.DTO.Request.*;
import com.graduation.GMS.DTO.Response.*;
import com.graduation.GMS.Handlers.HandleCurrentUserSession;
import com.graduation.GMS.Models.*;
import com.graduation.GMS.Models.Class;
import com.graduation.GMS.Models.Enums.Roles;
import com.graduation.GMS.Repositories.*;
import com.graduation.GMS.Services.GeneralServices.NotificationService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SessionService {

    private UserRepository userRepository;

    private SessionRepository sessionRepository;

    private ClassRepository classRepository;

    private User_SessionRepository userSessionRepository;

    private Session_AttendanceRepository sessionAttendanceRepository;

    private NotificationRepository notificationRepository;

    private NotificationService notificationService;

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
    public ResponseEntity<?> createSession(SessionRequest request) {
        if (sessionRepository.findByTitle(request.getTitle()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Session title already exists"));
        }
        Class aClass = classRepository.findById(request.getClassId())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "Class not found"));
        User coach = userRepository.findById(request.getCoachId())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Coach not found"));
        Session session = new Session();
        session.setTitle(request.getTitle());
        session.setDescription(request.getDescription());
        session.setMaxNumber(request.getMaxNumber());
        session.setCreatedAt(LocalDateTime.now());
        session.setCoach(coach);
        session.setAClass(aClass);
        // schedules
        if (request.getSchedules() != null && !request.getSchedules().isEmpty()) {
            List<SessionSchedule> schedules = request.getSchedules().stream()
                    .map(req -> {
                        SessionSchedule schedule = new SessionSchedule();
                        schedule.setDay(req.getDay());
                        schedule.setStartTime(req.getStartTime());
                        schedule.setEndTime(req.getEndTime());
                        schedule.setSession(session);
                        return schedule;
                    }).toList();
            session.setSchedules(schedules);
        }
        sessionRepository.save(session);

        // notification
        Notification notification = new Notification();
        notification.setTitle("New Session has been created" + session.getTitle());
        notification.setContent("Hurry up to join session: " + session.getTitle());
        notification.setCreatedAt(LocalDateTime.now());

        notification = notificationRepository.save(notification);

        List<User> users = userRepository.findAllByRoleName(Roles.User);
        notificationService.sendNotificationToUsers(users, notification);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Session created successfully"));
    }


    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
    public ResponseEntity<?> updateSession(Integer id, SessionRequest request) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session not found"));
        if (request.getTitle() != null && !request.getTitle().equals(session.getTitle())) {
            session.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            session.setDescription(request.getDescription());
        }
        if (request.getMaxNumber() != null) {
            session.setMaxNumber(request.getMaxNumber());
        }
        if (request.getCoachId() != null &&
                !session.getCoach().getId().equals(request.getCoachId())) {
            User coach = userRepository.findById(request.getCoachId())
                    .orElseThrow(() ->
                            new ResponseStatusException(HttpStatus.BAD_REQUEST, "Coach not found"));
            session.setCoach(coach);
        }
        if (request.getClassId() != null &&
                !session.getAClass().getId().equals(request.getClassId())) {

            Class aClass = classRepository.findById(request.getClassId())
                    .orElseThrow(() ->
                            new ResponseStatusException(HttpStatus.BAD_REQUEST, "Class not found"));
            session.setAClass(aClass);
        }
        if (request.getSchedules() != null) {
            session.getSchedules().clear();
            List<SessionSchedule> newSchedules = request.getSchedules().stream()
                    .map(req -> {
                        SessionSchedule schedule = new SessionSchedule();
                        schedule.setDay(req.getDay());
                        schedule.setStartTime(req.getStartTime());
                        schedule.setEndTime(req.getEndTime());
                        schedule.setSession(session);
                        return schedule;
                    }).toList();
            session.getSchedules().addAll(newSchedules);
        }
        sessionRepository.save(session);
        return ResponseEntity.ok(
                Map.of("message", "Session updated successfully"));
    }


    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
    public ResponseEntity<?> deleteSession(Integer id) {
        if (!sessionRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Session not found"));
        }

        sessionRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Session deleted successfully"));
    }

    public ResponseEntity<?> getSessionById(Integer id) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Session not found"
                        )
                );
        List<SessionResponse.Schedule> schedules =
                session.getSchedules()
                        .stream()
                        .map(s -> new SessionResponse.Schedule(
                                s.getDay().name(),
                                s.getStartTime(),
                                s.getEndTime()
                        ))
                        .toList();
        SessionResponse response = new SessionResponse(
                session.getId(),
                session.getTitle(),
                session.getDescription(),
                session.getAClass().getId(),
                UserResponse.mapToUserResponse(session.getCoach()),
                calculateRate(session.getId()),
                schedules,
                session.getCreatedAt(),
                session.getMaxNumber(),
                userSessionRepository.countBySession(session),
                null,
                null,
                null,
                session.getAClass().getName(),
                session.getAClass().getImagePath()
        );
        return ResponseEntity.ok(response);
    }


    public ResponseEntity<?> getAllSessions(Pageable pageable) {
        Page<Session> sessions = sessionRepository.findAllPageable(pageable);
        List<SessionResponse> sessionResponses = sessions.stream()
                .map(session -> {
                    List<SessionResponse.Schedule> schedules =
                            session.getSchedules()
                                    .stream()
                                    .map(s -> new SessionResponse.Schedule(
                                            s.getDay().name(),
                                            s.getStartTime(),
                                            s.getEndTime()
                                    ))
                                    .toList();
                    return new SessionResponse(
                            session.getId(),
                            session.getTitle(),
                            session.getDescription(),
                            session.getAClass().getId(),
                            UserResponse.mapToUserResponse(session.getCoach()),
                            calculateRate(session.getId()),
                            schedules,
                            session.getCreatedAt(),
                            session.getMaxNumber(),
                            userSessionRepository.countBySession(session),
                            null,
                            null,
                            null,
                            session.getAClass().getName(),
                            session.getAClass().getImagePath()
                    );
                })
                .toList();
        Map<String, Object> result = new HashMap<>();
        result.put("count", sessions.getTotalElements());
        result.put("totalPages", sessions.getTotalPages());
        result.put("currentPage", sessions.getNumber());
        result.put("sessions", sessionResponses);
        return ResponseEntity.ok(Map.of("message", result));
    }

    public ResponseEntity<?> getAllSessionsByClassId(int classId, Pageable pageable) {
        Page<Session> sessions = sessionRepository.findAllByAClass_Id(classId, pageable);
        if (sessions.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No sessions found"));
        }
        List<SessionResponse> sessionResponses =
                sessions.stream()
                        .map(session -> {
                            List<SessionResponse.Schedule> schedules =
                                    session.getSchedules()
                                            .stream()
                                            .map(s -> new SessionResponse.Schedule(
                                                    s.getDay().name(),
                                                    s.getStartTime(),
                                                    s.getEndTime()
                                            ))
                                            .toList();
                            return new SessionResponse(
                                    session.getId(),
                                    session.getTitle(),
                                    session.getDescription(),
                                    session.getAClass().getId(),
                                    UserResponse.mapToUserResponse(session.getCoach()),
                                    calculateRate(session.getId()),
                                    schedules,
                                    session.getCreatedAt(),
                                    session.getMaxNumber(),
                                    userSessionRepository.countBySession(session),
                                    null,
                                    null,
                                    null,
                                    session.getAClass().getName(),
                                    session.getAClass().getImagePath()
                            );
                        })
                        .toList();
        return ResponseEntity.ok(sessionResponses);
    }


    private Float calculateRate(int sessionId) {
        // First check if the session exists
        Optional<Session> sessionOptional = sessionRepository.findById(sessionId);
        if (sessionOptional.isEmpty()) {
            return 0.0F;
        }

        // Get all User_Session entries for this session
        List<User_Session> userSessions = userSessionRepository.findBySession(sessionOptional.get());

        if (userSessions.isEmpty()) {
            return 0.0F;
        }

        // Calculate average rating using stream API
        Double average = userSessions.stream()
                .filter(up -> up.getRate() != null) // Filter out null ratings
                .mapToDouble(User_Session::getRate) // Convert to double for calculation
                .average() // Calculate average
                .orElse(0.0); // Default to 0.0 if no ratings

        return average.floatValue(); // Convert back to Float
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
    public ResponseEntity<?> assignSessionToUser(AssignSessionToUserRequest request) {
        User user = userRepository.findById(request.getUserId()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "User not found"));
        }

        Session session = sessionRepository.findById(request.getSessionId()).orElse(null);
        if (session == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Session not found"));
        }

        // Check if max number of users reached
        int currentSubscribers = userSessionRepository.countBySession(session);
        if (currentSubscribers >= session.getMaxNumber()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Session is full. Max number of users reached."));
        }

        // Check if already assigned
        boolean exists = userSessionRepository.existsByUserAndSession(user, session);
        if (exists) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Session is already assigned to this user"));
        }

        // Create and save the relationship
        User_Session userSession = new User_Session();
        userSession.setSession(session);
        userSession.setUser(user);
        userSession.setIsActive(true);
        userSession.setJoinedAt(LocalDateTime.now());
        userSessionRepository.save(userSession);

        // Create and send notification
        Notification notification = new Notification();
        notification.setTitle("New Subscription");
        notification.setContent("New Subscription has been made in Session:" + userSession.getSession().getTitle());
        notification.setCreatedAt(LocalDateTime.now());
        // Persist notification first
        notification = notificationRepository.save(notification);

        notificationService.sendNotification(
                user,
                notification
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "session successfully assigned to user"));
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
    public ResponseEntity<?> unAssignSessionToUser(AssignSessionToUserRequest request) {
        User user = userRepository.findById(request.getUserId()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "User not found"));
        }

        Session session = sessionRepository.findById(request.getSessionId()).orElse(null);
        if (session == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Session not found"));
        }

        // Find the existing assignment
        Optional<User_Session> userSessionOptional = userSessionRepository.findByUserAndSession(user, session);
        if (userSessionOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Session is not assigned to this user"));
        }

        // Delete the assignment
        userSessionRepository.delete(userSessionOptional.get());

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Session successfully unassigned from user"));
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('User')")
    public ResponseEntity<?> rateSession(RateSessionRequest request) {
        User user = HandleCurrentUserSession.getCurrentUser();
        // Validate session exists
        var session = sessionRepository.findById(request.getSessionId()).orElse(null);
        if (session == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Session not found"));
        }
        // Check if session is assigned to user
        if (!userSessionRepository.existsByUserAndSession(user, session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You can only rate sessions assigned to you"));
        }
        // Validate rating (1-5)
        if (request.getRate() == null || request.getRate() < 1 || request.getRate() > 5) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Rating must be between 1 and 5"));
        }
        // Find user-session relationship
        User_Session userSession = userSessionRepository.findByUserAndSession(user, session)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        // Update rating
        userSession.setRate(request.getRate());
        userSessionRepository.save(userSession);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Session rated successfully"));
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('User')")
    public ResponseEntity<?> submitFeedback(FeedBackSessionRequest request) {

        User user = HandleCurrentUserSession.getCurrentUser();

        // Validate session exists
        Optional<Session> sessionOptional = sessionRepository.findById(request.getSessionId());
        if (sessionOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Session not found"));
        }

        Session session = sessionOptional.get();

        // Check if session is assigned to user
        if (!userSessionRepository.existsByUserAndSession(user, session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You can only submit feedback for sessions assigned to you"));
        }

        // Validate feedback not empty
        if (request.getFeedback() == null || request.getFeedback().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Feedback cannot be empty"));
        }

        // Find user-session relationship
        User_Session userSession = userSessionRepository.findByUserAndSession(user, session)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        // Update feedback
        userSession.setFeedback(request.getFeedback());
        userSessionRepository.save(userSession);

        return ResponseEntity.ok(Map.of("message", "Feedback submitted successfully"));
    }

    public ResponseEntity<?> getAllSessionFeedBacks(Integer sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));
        // feedbacks
        List<UserFeedBackResponse> feedBacks =
                userSessionRepository.findFeedbackBySession(session)
                        .stream()
                        .map(us -> new UserFeedBackResponse(
                                us.getUser(),
                                us.getFeedback()
                        ))
                        .toList();
        // schedules (day + time)
        List<SessionResponse.Schedule> schedules =
                session.getSchedules()
                        .stream()
                        .map(s -> new SessionResponse.Schedule(
                                s.getDay().name(),
                                s.getStartTime(),
                                s.getEndTime()
                        ))
                        .toList();
        SessionResponse response = new SessionResponse(
                session.getId(),
                session.getTitle(),
                session.getDescription(),
                session.getAClass().getId(),
                UserResponse.mapToUserResponse(session.getCoach()),
                calculateRate(session.getId()),
                schedules,
                session.getCreatedAt(),
                session.getMaxNumber(),
                userSessionRepository.countBySession(session),
                feedBacks,
                null,
                null,
                session.getAClass().getName(),
                session.getAClass().getImagePath()
        );
        return ResponseEntity.ok(response);
    }


    @PreAuthorize("hasAnyAuthority('User')")
    public ResponseEntity<?> getMyAssignedSessions() {
        User user = HandleCurrentUserSession.getCurrentUser();
        List<User_Session> userSessions = userSessionRepository.findByUser(user);
        List<SessionResponse> sessionResponses = userSessions.stream()
                .map(us -> {
                    Session session = us.getSession();
                    // schedules (day + time)
                    List<SessionResponse.Schedule> schedules =
                            session.getSchedules()
                                    .stream()
                                    .map(s -> new SessionResponse.Schedule(
                                            s.getDay().name(),
                                            s.getStartTime(),
                                            s.getEndTime()
                                    ))
                                    .toList();
                    return new SessionResponse(
                            session.getId(),
                            session.getTitle(),
                            session.getDescription(),
                            session.getAClass().getId(),
                            UserResponse.mapToUserResponse(session.getCoach()),
                            us.getRate(),
                            schedules,
                            session.getCreatedAt(),
                            session.getMaxNumber(),
                            userSessionRepository.countBySession(session),
                            null,
                            us.getFeedback(),
                            us.getJoinedAt(),
                            session.getAClass().getName(),
                            session.getAClass().getImagePath()
                    );
                })
                .toList();
        return ResponseEntity.ok(sessionResponses);
    }


    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
    public ResponseEntity<?> getAssignedSessionsByUserId(Integer userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "User not found"));
        }
        User user = userOptional.get();
        List<User_Session> userSessions = userSessionRepository.findByUser(user);
        List<SessionResponse> sessionResponses = userSessions.stream()
                .map(us -> {
                    Session session = us.getSession();
                    // schedules (day + start/end time)
                    List<SessionResponse.Schedule> schedules =
                            session.getSchedules()
                                    .stream()
                                    .map(s -> new SessionResponse.Schedule(
                                            s.getDay().name(),
                                            s.getStartTime(),
                                            s.getEndTime()
                                    ))
                                    .toList();
                    return new SessionResponse(
                            session.getId(),
                            session.getTitle(),
                            session.getDescription(),
                            session.getAClass().getId(),
                            UserResponse.mapToUserResponse(session.getCoach()),
                            us.getRate(),
                            schedules,
                            session.getCreatedAt(),
                            session.getMaxNumber(),
                            userSessionRepository.countBySession(session),
                            null,
                            us.getFeedback(),
                            us.getJoinedAt(),
                            session.getAClass().getName(),
                            session.getAClass().getImagePath()
                    );
                })
                .toList();
        return ResponseEntity.ok(sessionResponses);
    }


    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Secretary')")
    public ResponseEntity<?> createAttendanceFromQr(QrSessionAttendanceRequest request) {
        try {
            // 1. Decode QR Base64 image and extract email text
            String email = decodeEmailFromQrImage(request.getQrCode());

            // 2. Find user by extracted email
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "User not found for the QR code"));
            }

            Optional<Session> session = sessionRepository.findById(request.getSessionId());
            if (session.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Session not found"));
            }

            User user = userOptional.get();

            // 3. Check if attendance for today already exists
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

            boolean attendanceExists = sessionAttendanceRepository.existsByUserAndDateBetween(user, startOfDay,
                    endOfDay);
            if (attendanceExists) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("message", "Attendance for today is already recorded"));
            }

            // 4. Create and save attendance
            Session_Attendance attendance = new Session_Attendance();
            attendance.setUser(user);
            attendance.setSession(session.get());
            attendance.setDate(LocalDateTime.now());

            sessionAttendanceRepository.save(attendance);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Attendance recorded successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Internal server error: " + e.getMessage()));
        }
    }

    // Helper method to decode Base64 QR image and extract email
    private String decodeEmailFromQrImage(String qrBase64) throws Exception {
        return "result.getText()";
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Secretary')")
    public ResponseEntity<?> getUserAttendanceById(Integer userId, Integer sessionId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        }

        User user = userOptional.get();

        Optional<Session> sessionOptional = sessionRepository.findById(sessionId);
        if (sessionOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Session not found"));
        }

        Session session = sessionOptional.get();
        // Fetch last 30 attendance records sorted descending by date
        List<Session_Attendance> attendanceList = sessionAttendanceRepository
                .findTop30ByUserAndSessionOrderByDateDesc(user, session);

        // Format dates as ISO strings
        List<String> attendanceDates = attendanceList.stream()
                .map(att -> att.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .toList();

        UserResponse userResponse = UserResponse.mapToUserResponse(user);

        Map<String, Object> response = Map.of(
                "user", userResponse,
                "attendances", attendanceDates);

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyAuthority('Admin','Secretary,Coach')")
    public ResponseEntity<?> getSessionSubscribers(Integer sessionId) {
        var session = sessionRepository.findById(sessionId).orElse(null);
        if (session == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "no Session with this id"));
        List<User_Session> targetSession = userSessionRepository.findBySession(session);
        List<UserResponse> sessionSubscribers = new ArrayList<>();
        if (!targetSession.isEmpty())
            for (User_Session item : targetSession)
                sessionSubscribers.add(UserResponse.mapToUserResponse(item.getUser()));
        Map<Integer, Boolean> subscribersStatus = targetSession.stream()
                .collect(Collectors.toMap(
                        up -> up.getUser().getId(),
                        User_Session::getIsActive
                ));
        return ResponseEntity.ok(Map.of(
                "subscribers", sessionSubscribers,
                "subscribersStatus", subscribersStatus
        ));
    }

    public ResponseEntity<?> deleteSessionFeedBack(Integer userId, Integer classId) {
        var user = userRepository.findById(userId).orElse(null);
        var session = sessionRepository.findById(classId).orElse(null);
        if (user == null || session == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "wrong user or session Id"));
        }
        var userSession = userSessionRepository.findByUserAndSession(user, session).orElse(null);
        if (userSession == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "user has no subscription"));
        }
        userSession.setFeedback(null);
        userSessionRepository.save(userSession);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "feedback deleted"));
    }

    @PreAuthorize("hasAnyAuthority('Admin','Coach','Secretary')")
    public ResponseEntity<?> getUserSubscriptionSessions(Integer userId) {
        var user = userRepository.findById(userId).orElse(null);
        if (user == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "user with this id not found"));
        List<User_Session> userSubscriptions = userSessionRepository.findByUser(user);
        List<SessionResponse> subscriptionSessions = new ArrayList<>();
        if (!userSubscriptions.isEmpty())
            for (User_Session item : userSubscriptions)
                subscriptionSessions.add(SessionResponse.builder().title(item.getSession().getTitle()).build());
        return ResponseEntity.status(HttpStatus.OK).body(subscriptionSessions);
    }

}
