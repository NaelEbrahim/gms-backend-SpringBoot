package com.graduation.GMS.Services;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.graduation.GMS.DTO.Request.*;
import com.graduation.GMS.DTO.Response.ProgramResponse;
import com.graduation.GMS.DTO.Response.SessionResponse;
import com.graduation.GMS.DTO.Response.UserFeedBackResponse;
import com.graduation.GMS.DTO.Response.UserResponse;
import com.graduation.GMS.Handlers.HandleCurrentUserSession;
import com.graduation.GMS.Models.*;
import com.graduation.GMS.Models.Class;
import com.graduation.GMS.Models.Enums.WeekDay;
import com.graduation.GMS.Repositories.*;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
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

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
    public ResponseEntity<?> createSession(SessionRequest request) {
        // Check if session title already exists
        Optional<Session> existingSession = sessionRepository.findByTitle(request.getTitle());
        if (existingSession.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Session title already exists"));
        }

        Optional<Class> classOptional = classRepository.findById(request.getClassId());
        if (classOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Class not found"));
        }

        // 🔁 Convert input strings to WeekDay enum safely
        List<WeekDay> weekDays;
        try {
            weekDays = request.getDays().stream()
                    .map(day -> WeekDay.valueOf(day))
                    .toList();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message",
                            "Invalid day name. Expected values: Sunday, Monday, Tuesday, Wednesday, Thursday, Friday, Saturday"));
        }

        Optional<User> userOptional = userRepository.findById(request.getCoachId());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        }

        // Create and save new session
        Session session = new Session();
        session.setTitle(request.getTitle());
        session.setDescription(request.getDescription());
        session.setMaxNumber(request.getMaxNumber());
        session.setStartTime(request.getStartTime());
        session.setEndTime(request.getEndTime());
        session.setCreatedAt(LocalDateTime.now());
        session.setCoach(userOptional.get());
        session.setAClass(classOptional.get());
        if (!weekDays.isEmpty()) {
            String daysString = weekDays.stream()
                    .map(Enum::name)
                    .collect(Collectors.joining(","));
            session.setDays(daysString);
        }

        // ✅ Set the parsed enum days here

        sessionRepository.save(session);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Session created successfully"));
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
    public ResponseEntity<?> updateSession(Integer id, SessionRequest request) {
        Optional<Session> optionalSession = sessionRepository.findById(id);
        if (optionalSession.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Session not found"));
        }

        Session session = optionalSession.get();

        Optional<Class> classOptional = classRepository.findById(request.getClassId());
        if (classOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Class not found"));
        }

        Optional<User> userOptional = userRepository.findById(request.getCoachId());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        }

        if(!session.getCoach().getId().equals(request.getCoachId())) {
            session.setCoach(userOptional.get());
        }

        // 🔁 Convert input strings to WeekDay enum safely
        List<WeekDay> weekDays;
        try {
            weekDays = request.getDays().stream()
                    .map(day -> WeekDay.valueOf(day))
                    .toList();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message",
                            "Invalid day name. Expected values: Sunday, Monday, Tuesday, Wednesday, Thursday, Friday, Saturday"));
        }

        if (!session.getTitle().equals(request.getTitle()) && !request.getTitle().isEmpty()) {
            session.setTitle(request.getTitle());
        }
        if (request.getDescription() != null && !session.getDescription().equals(request.getDescription())) {
            session.setDescription(request.getDescription());
        }
        if (request.getMaxNumber() != null && !session.getMaxNumber().equals(request.getMaxNumber())) {
            session.setMaxNumber(request.getMaxNumber());
        }
        // update the list here
        if (request.getStartTime() != null && !session.getStartTime().equals(request.getStartTime())) {
            session.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null && !session.getEndTime().equals(request.getEndTime())) {
            session.setEndTime(request.getEndTime());
        }
        if (request.getClassId() != null && !session.getAClass().getId().equals(request.getClassId())) {
            session.setAClass(classRepository.findById(request.getClassId()).get());
        }
        if (!weekDays.isEmpty()) {
            String daysString = weekDays.stream()
                    .map(Enum::name)
                    .collect(Collectors.joining(","));
            session.setDays(daysString);
        }

        sessionRepository.save(session);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Session updated successfully"));
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
        Optional<Session> sessionOptional = sessionRepository.findById(id);
        if (sessionOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Session not found"));
        }

        Session session = sessionOptional.get();

        List<String> days = new ArrayList<>();
        if (session.getDays() != null && !session.getDays().isEmpty()) {
            days = Arrays.stream(session.getDays().split(","))
                    .map(String::trim)
                    .toList();
        }

        SessionResponse response = new SessionResponse(
                session.getId(),
                session.getTitle(),
                session.getDescription(),
                session.getAClass().getId(),
                UserResponse.mapToUserResponse(session.getCoach()),
                calculateRate(session.getId()),
                days,
                session.getCreatedAt(),
                session.getStartTime(),
                session.getEndTime(),
                session.getMaxNumber(),
                userSessionRepository.countBySession(session),
                null);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    public ResponseEntity<?> getAllSessions() {
        List<Session> sessions = sessionRepository.findAll();

        if (sessions.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No sessions found"));
        }

        List<SessionResponse> sessionResponses = sessions.stream()
                .map(session -> {
                    // Get workouts for each session

                    List<String> days = new ArrayList<>();
                    if (session.getDays() != null && !session.getDays().isEmpty()) {
                        days = Arrays.stream(session.getDays().split(","))
                                .map(String::trim)
                                .toList();
                    }

                    return new SessionResponse(
                            session.getId(),
                            session.getTitle(),
                            session.getDescription(),
                            session.getAClass().getId(),
                            UserResponse.mapToUserResponse(session.getCoach()),
                            calculateRate(session.getId()),
                            days,
                            session.getCreatedAt(),
                            session.getStartTime(),
                            session.getEndTime(),
                            session.getMaxNumber(),
                            userSessionRepository.countBySession(session),
                            null);
                })
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK).body(sessionResponses);
    }

    public ResponseEntity<?> getAllSessionsByClassId(int classId) {
        List<Session> sessions = sessionRepository.findAll();

        if (sessions.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No sessions found"));
        }

        List<SessionResponse> sessionResponses = sessions.stream()
                .filter(session -> session.getAClass().getId() == classId)
                .map(session -> {
                    // Get workouts for each session

                    List<String> days = new ArrayList<>();
                    if (session.getDays() != null && !session.getDays().isEmpty()) {
                        days = Arrays.stream(session.getDays().split(","))
                                .map(String::trim)
                                .toList();
                    }

                    return new SessionResponse(
                            session.getId(),
                            session.getTitle(),
                            session.getDescription(),
                            session.getAClass().getId(),
                            UserResponse.mapToUserResponse(session.getCoach()),
                            calculateRate(session.getId()),
                            days,
                            session.getCreatedAt(),
                            session.getStartTime(),
                            session.getEndTime(),
                            session.getMaxNumber(),
                            userSessionRepository.countBySession(session),
                            null);
                })
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK).body(sessionResponses);
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
        Optional<User> userOptional = userRepository.findById(request.getUserId());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        }

        Optional<Session> sessionOptional = sessionRepository.findById(request.getSessionId());
        if (sessionOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Session not found"));
        }

        Session session = sessionOptional.get();
        User user = userOptional.get();

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
        userSession.setJoinedAt(LocalDateTime.now());
        userSessionRepository.save(userSession);

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "session successfully assigned to user"));
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
    public ResponseEntity<?> unAssignSessionToUser(AssignSessionToUserRequest request) {
        Optional<User> userOptional = userRepository.findById(request.getUserId());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        }

        Optional<Session> sessionOptional = sessionRepository.findById(request.getSessionId());
        if (sessionOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Session not found"));
        }

        Session session = sessionOptional.get();
        User user = userOptional.get();

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
        Optional<Session> sessionOptional = sessionRepository.findById(request.getSessionId());
        if (sessionOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Session not found"));
        }

        Session session = sessionOptional.get();

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

        // Find or create user-session relationship
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
        Optional<Session> sessionOptional = sessionRepository.findById(sessionId);
        if (sessionOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Session not found"));
        }

        Session session = sessionOptional.get();

        // Get only active subscribers
        List<UserFeedBackResponse> feedBacks = userSessionRepository.findFeedbackBySession(session)
                .stream()
                .map(User_Session -> {
                    User user = User_Session.getUser();
                    return new UserFeedBackResponse(user, User_Session.getFeedback());
                })
                .toList();

        List<String> days = new ArrayList<>();
        if (session.getDays() != null && !session.getDays().isEmpty()) {
            days = Arrays.stream(session.getDays().split(","))
                    .map(String::trim)
                    .toList();
        }

        SessionResponse responseDto = new SessionResponse(
                session.getId(),
                session.getTitle(),
                session.getDescription(),
                session.getAClass().getId(),
                UserResponse.mapToUserResponse(session.getCoach()),
                calculateRate(session.getId()),
                days,
                session.getCreatedAt(),
                session.getStartTime(),
                session.getEndTime(),
                session.getMaxNumber(),
                userSessionRepository.countBySession(session),
                feedBacks);

        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @PreAuthorize("hasAnyAuthority('User')")
    public ResponseEntity<?> getMyAssignedSessions() {
        User user = HandleCurrentUserSession.getCurrentUser();

        List<User_Session> userSessions = userSessionRepository.findByUser(user);

        if (userSessions.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No sessions assigned to this user"));
        }

        List<SessionResponse> sessionResponses = userSessions.stream()
                .map(up -> {
                    Session session = up.getSession();

                    List<String> days = new ArrayList<>();
                    if (session.getDays() != null && !session.getDays().isEmpty()) {
                        days = Arrays.stream(session.getDays().split(","))
                                .map(String::trim)
                                .toList();
                    }

                    return new SessionResponse(
                            session.getId(),
                            session.getTitle(),
                            session.getDescription(),
                            session.getAClass().getId(),
                            UserResponse.mapToUserResponse(session.getCoach()),
                            calculateRate(session.getId()),
                            days,
                            session.getCreatedAt(),
                            session.getStartTime(),
                            session.getEndTime(),
                            session.getMaxNumber(),
                            userSessionRepository.countBySession(session),
                            null);
                })
                .toList();

        return ResponseEntity.ok(sessionResponses);
    }

    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
    public ResponseEntity<?> getAssignedSessionsByUserId(Integer userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        }

        User user = userOptional.get();
        List<User_Session> userSessions = userSessionRepository.findByUser(user);

        if (userSessions.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No sessions assigned to this user"));
        }

        List<SessionResponse> sessionResponses = userSessions.stream()
                .map(up -> {
                    Session session = up.getSession();

                    List<String> days = new ArrayList<>();
                    if (session.getDays() != null && !session.getDays().isEmpty()) {
                        days = Arrays.stream(session.getDays().split(","))
                                .map(String::trim)
                                .toList();
                    }

                    return new SessionResponse(
                            session.getId(),
                            session.getTitle(),
                            session.getDescription(),
                            session.getAClass().getId(),
                            UserResponse.mapToUserResponse(session.getCoach()),
                            calculateRate(session.getId()),
                            days,
                            session.getCreatedAt(),
                            session.getStartTime(),
                            session.getEndTime(),
                            session.getMaxNumber(),
                            userSessionRepository.countBySession(session),
                            null);
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

        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "QR code could not be decoded"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Internal server error: " + e.getMessage()));
        }
    }

    // Helper method to decode Base64 QR image and extract email
    private String decodeEmailFromQrImage(String qrBase64) throws Exception {
        byte[] imageBytes = Base64.getDecoder().decode(qrBase64);
        ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
        BufferedImage bufferedImage = ImageIO.read(bis);

        LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        Result result = new MultiFormatReader().decode(bitmap);
        return result.getText();
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

}
