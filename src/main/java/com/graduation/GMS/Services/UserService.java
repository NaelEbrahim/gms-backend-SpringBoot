package com.graduation.GMS.Services;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.graduation.GMS.Config.SecurityConfig;
import com.graduation.GMS.DTO.Request.*;
import com.graduation.GMS.DTO.Response.*;
import com.graduation.GMS.Handlers.HandleCurrentUserSession;
import com.graduation.GMS.Models.*;
import com.graduation.GMS.Models.Enums.Roles;
import com.graduation.GMS.Repositories.*;
import com.graduation.GMS.Services.GeneralServices.JwtService;
import com.graduation.GMS.Services.GeneralServices.NotificationService;
import com.graduation.GMS.Tools.BMI_Calculator;
import com.graduation.GMS.Tools.FilesManagement;
import com.graduation.GMS.Tools.Generators;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final AuthTokenRepository authTokenRepository;

    private final RoleRepository roleRepository;

    private final User_RoleRepository userRoleRepository;

    private final SecurityConfig securityConfig;

    private final JwtService jwtService;

    private final PrivateCoachRepository privateCoachRepository;

    private final AttendanceRepository attendanceRepository;

    private final HealthInfoRepository healthInfoRepository;

    private final NotificationRepository notificationRepository;

    private final NotificationService notificationService;

    private final Program_WorkoutRepository programWorkoutRepository;

    private final UserProgressRepository userProgressRepository;

    private final ProgramRepository programRepository;

    private final WorkoutRepository workoutRepository;


    @PreAuthorize("hasAnyAuthority('Admin','Secretary')")
    public ResponseEntity<?> createUser(UserRequest createRequest) throws Exception {
        return internalCreateUser(createRequest);
    }

    @Transactional
    public ResponseEntity<?> internalCreateUser(UserRequest createRequest) throws Exception {
        if (userRepository.findByEmail(createRequest.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message:", "email already exist"));
        }
        if (userRepository.findByPhoneNumber(createRequest.getPhoneNumber()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message:", "phoneNumber already exist"));
        }
        // Create User
        User newUser = new User();
        String password = (createRequest.getPassword() == null) ? Generators.generatePassword() : createRequest.getPassword();
        newUser.setFirstName(createRequest.getFirstName());
        newUser.setLastName(createRequest.getLastName());
        newUser.setEmail(createRequest.getEmail());
        newUser.setGender(createRequest.getGender());
        newUser.setDob(createRequest.getDob());
        newUser.setPhoneNumber(createRequest.getPhoneNumber());
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setQr(Generators.generateQRCode(createRequest.getEmail()));
        newUser.setPassword(securityConfig.passwordEncoder().encode(password));
        userRepository.save(newUser);
        //Roles
        for (Roles roleName : createRequest.getRoles()) {
            Role role = roleRepository.findByRoleName(roleName)
                    .orElseGet(() -> roleRepository.save(Role.builder().roleName(roleName).build()));
            userRoleRepository.save(User_Role.builder()
                    .user(newUser)
                    .role(role)
                    .build());
        }
        UserWithPasswordResponse userResponse = new UserWithPasswordResponse(newUser, password);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", userResponse));
    }

    @Transactional
    public ResponseEntity<?> userLogin(LoginRequest loginRequest, HttpServletResponse response) {
        var user = userRepository.findByEmail(loginRequest.getEmail()).orElse(null);
        if (user != null && securityConfig.passwordEncoder().matches(loginRequest.getPassword(), user.getPassword())) {
            //Fetch User Roles
            var ur = userRoleRepository.findByUserId(user.getId());
            List<Roles> userRoles = new ArrayList<>();
            for (User_Role element : ur)
                userRoles.add(element.getRole().getRoleName());
            // Tokens
            invalidateUserToken(user.getId());
            String accessToken = jwtService.generateAccessToken(user, userRoles);
            authTokenRepository.save(AuthToken.builder().user(user).accessToken(accessToken).build());
            String refreshToken = jwtService.generateRefreshToken(user);
            var cookie = new Cookie("refreshToken", refreshToken);
            cookie.setSecure(true);
            cookie.setHttpOnly(true);
            cookie.setMaxAge(259200);
            cookie.setPath("/auth/refresh");
            response.addCookie(cookie);
            ProfileResponse userResponse = new ProfileResponse(user, null, accessToken);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(Map.of("message", userResponse));
        } else
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "invalid username or password"));
    }

    @Transactional
    public ResponseEntity<?> updateProfile(Integer userId, UpdateProfileRequest userRequest) {
        var user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message:", "user not found"));
        }
        if (userRequest.getFirstName() != null && !userRequest.getFirstName().equalsIgnoreCase(user.getFirstName())) {
            user.setFirstName(userRequest.getFirstName());
        }
        if (userRequest.getLastName() != null && !userRequest.getLastName().equalsIgnoreCase(user.getLastName())) {
            user.setLastName(userRequest.getLastName());
        }
        if (userRequest.getEmail() != null && !userRequest.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmail(userRequest.getEmail()).isPresent())
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message:", "email already exist"));
            user.setEmail(userRequest.getEmail());
            try {
                user.setQr(Generators.generateQRCode(userRequest.getEmail()));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("message:", e.getMessage()));
            }
        }
        if (userRequest.getPhoneNumber() != null && !userRequest.getPhoneNumber().equals(user.getPhoneNumber())) {
            if (userRepository.findByPhoneNumber(userRequest.getPhoneNumber()).isPresent())
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message:", "phone number already exist"));
            user.setPhoneNumber(userRequest.getPhoneNumber());
        }
        if (userRequest.getDob() != null && !userRequest.getDob().equals(user.getDob())) {
            user.setDob(userRequest.getDob());
        }
        if (userRequest.getGender() != null && !userRequest.getGender().equals(user.getGender())) {
            user.setGender(userRequest.getGender());
        }
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message:", "information updated successfully"));
    }

    @Transactional
    public ResponseEntity<?> resetPassword(ResetPasswordRequest resetPasswordRequest) {
        var user = HandleCurrentUserSession.getCurrentUser();
        if (securityConfig.passwordEncoder().matches(resetPasswordRequest.getOldPassword(), user.getPassword())) {
            user.setPassword(securityConfig.passwordEncoder().encode(resetPasswordRequest.getNewPassword()));
            userRepository.save(user);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(Map.of("message:", "password updated successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message:", "old password incorrect"));
        }
    }

    @Transactional
    public ResponseEntity<?> logout() {
        var user = HandleCurrentUserSession.getCurrentUser();
        if (user != null) {
            invalidateUserToken(user.getId());
            SecurityContextHolder.clearContext();
            return ResponseEntity.status(HttpStatus.OK)
                    .body(Map.of("message", "logout successfully"));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "something went wrong"));
    }

    @Transactional
    public void invalidateUserToken(Integer userId) {
        authTokenRepository.deleteByUserId(userId);
        authTokenRepository.flush();
    }

    @PreAuthorize("hasAnyAuthority('Admin')")
    public ResponseEntity<?> getUsersByRoleWithSearch(Roles roleName, String keyword, Pageable pageable) {
        Page<User> usersPage = userRepository.searchByRoleAndKeyword(roleName, keyword, pageable);

        List<ProfileResponse> userResponses = usersPage
                .stream()
                .map(ProfileResponse::mapToProfileResponse)
                .toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("role", roleName);
        response.put("count", usersPage.getTotalElements());
        response.put("totalPages", usersPage.getTotalPages());
        response.put("currentPage", usersPage.getNumber());
        response.put("users", userResponses);

        return ResponseEntity.ok(response);
    }


    @PreAuthorize("hasAnyAuthority('Admin')")
    public ResponseEntity<?> getAll() {
        List<ProfileResponse> userResponses = userRepository.findAll()
                .stream()
                .map(u -> ProfileResponse.mapToProfileResponse(u))
                .toList();
        Map<String, Object> response = Map.of(
                "count", userResponses.size(),

                "users", userResponses
        );
        return ResponseEntity.ok(response);

    }

    private boolean userHasRole(User user, Roles roleName) {
        return userRoleRepository.findByUserId(user.getId()).stream()
                .anyMatch(ur -> roleName.name().equalsIgnoreCase(String.valueOf(ur.getRole().getRoleName())));
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Secretary')")
    public ResponseEntity<?> assignPrivateCoachToUser(AssignPrivateCoachToUserRequest request) {
        Optional<User> userOptional = userRepository.findById(request.getUserId());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        }

        Optional<User> coachOptional = userRepository.findById(request.getCoachId());
        if (coachOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Coach not found"));
        }

        User coach = coachOptional.get();
        User user = userOptional.get();

        // Check if already assigned
        boolean exists = privateCoachRepository.existsByUserAndCoach(user, coach);
        if (exists) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Coach is already assigned to this user"));
        }

        if (!userHasRole(coach, Roles.Coach)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Selected user is not a coach"));
        }

        // Create and save the relationship
        PrivateCoach userCoach = new PrivateCoach();
        userCoach.setUser(user);
        userCoach.setCoach(coach);
        userCoach.setStartedAt(LocalDateTime.now());
        userCoach.setPricePerMonth(request.getPaymentAmount());

        privateCoachRepository.save(userCoach);

        // Create and send notification
        Notification notification = new Notification();
        notification.setTitle("Private Coach Assigned");
        notification.setContent("New Private Coach has been assigned to monitor you...Coach" +
                coach.getFirstName() + " " + coach.getLastName());
        notification.setCreatedAt(LocalDateTime.now());
        // Persist notification first
        notification = notificationRepository.save(notification); // Save and get managed instance

        notificationService.sendNotification(
                userOptional.get(),
                notification
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Coach successfully assigned to user"));
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Secretary')")
    public ResponseEntity<?> updateAssignPrivateCoachToUser(AssignPrivateCoachToUserRequest request) {
        Optional<User> userOptional = userRepository.findById(request.getUserId());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        }

        Optional<User> coachOptional = userRepository.findById(request.getCoachId());
        if (coachOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Coach not found"));
        }

        User coach = coachOptional.get();
        User user = userOptional.get();

        if (!userHasRole(coach, Roles.Coach)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Selected user is not a coach"));
        }

        // Find the existing assignment
        Optional<PrivateCoach> userCoachOptional = privateCoachRepository.findByUserAndCoach(user, coach);
        if (userCoachOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Coach is not assigned to this user"));
        }

        // Create and save the relationship
        PrivateCoach userCoach = userCoachOptional.get();

        userCoach.setStartedAt(LocalDateTime.now());
        userCoach.setPricePerMonth(request.getPaymentAmount());

        privateCoachRepository.save(userCoach);

        // Create and send notification
        Notification notification = new Notification();
        notification.setTitle("Update Private Coach Assigned");
        notification.setContent("Private Coach has been assigned to monitor you...Coach" +
                coach.getFirstName() + " " + coach.getLastName());
        notification.setCreatedAt(LocalDateTime.now());
        // Persist notification first
        notification = notificationRepository.save(notification); // Save and get managed instance

        notificationService.sendNotification(
                userOptional.get(),
                notification
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Coach successfully Update assigned to user"));
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Secretary')")
    public ResponseEntity<?> unAssignCoachToUser(UnAssignPrivateCoachToUserRequest request) {
        Optional<User> userOptional = userRepository.findById(request.getUserId());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        }

        Optional<User> coachOptional = userRepository.findById(request.getCoachId());
        if (coachOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Coach not found"));
        }

        User coach = coachOptional.get();
        User user = userOptional.get();

        if (!userHasRole(coach, Roles.Coach)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Selected user is not a coach"));
        }

        // Find the existing assignment
        Optional<PrivateCoach> userCoachOptional = privateCoachRepository.findByUserAndCoach(user, coach);
        if (userCoachOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Coach is not assigned to this user"));
        }

        // Delete the assignment
        privateCoachRepository.delete(userCoachOptional.get());

        // Create and send notification
        Notification notification = new Notification();
        notification.setTitle("Private Coach UnAssigned");
        notification.setContent("Private Coach has been Un assigned from you...Coach" +
                coach.getFirstName() + " " + coach.getLastName());
        notification.setCreatedAt(LocalDateTime.now());
        // Persist notification first
        notification = notificationRepository.save(notification); // Save and get managed instance

        notificationService.sendNotification(
                userOptional.get(),
                notification
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Coach successfully unassigned from user"));
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Secretary')")
    public ResponseEntity<?> createAttendanceFromQr(QrAttendanceRequest request) {
        try {
            // 1. Decode QR Base64 image and extract email text
            String email = decodeEmailFromQrImage(request.getQrCode());

            // 2. Find user by extracted email
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "User not found for the QR code"));
            }

            User user = userOptional.get();

            // 3. Check if attendance for today already exists
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

            boolean attendanceExists = attendanceRepository.existsByUserAndDateBetween(user, startOfDay, endOfDay);
            if (attendanceExists) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("message", "Attendance for today is already recorded"));
            }

            // 4. Create and save attendance
            Attendance attendance = new Attendance();
            attendance.setUser(user);
            attendance.setDate(LocalDateTime.now());

            attendanceRepository.save(attendance);

            // Create and send notification
            Notification notification = new Notification();
            notification.setTitle("Attendance Notification");
            notification.setContent("Thank you for your attendance");
            notification.setCreatedAt(LocalDateTime.now());
            // Persist notification first
            notification = notificationRepository.save(notification); // Save and get managed instance

            notificationService.sendNotification(
                    userOptional.get(),
                    notification
            );

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
    public ResponseEntity<?> getUserAttendanceById(Integer userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        }

        User user = userOptional.get();

        // Fetch last 30 attendance records sorted descending by date
        List<Attendance> attendanceList = attendanceRepository
                .findTop30ByUserOrderByDateDesc(user);

        // Map attendances to list of LocalDateTime (or formatted string if you prefer)
        List<LocalDateTime> attendanceDates = attendanceList.stream()
                .map(Attendance::getDate)
                .toList();

        // Map user entity to user response DTO (implement this mapping according to your UserResponse)
        UserResponse userResponse = UserResponse.mapToUserResponse(user);

        Map<String, Object> response = Map.of(
                "user", userResponse,
                "attendances", attendanceDates
        );

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> getUserProfile() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", ProfileResponse.mapToProfileResponse(HandleCurrentUserSession.getCurrentUser())));
    }

    public ResponseEntity<?> getUserQR() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", HandleCurrentUserSession.getCurrentUser().getQr()));
    }

    @Transactional
    public ResponseEntity<?> uploadUserProfileImage(ImageRequest request) {
        Optional<User> user = userRepository.findById(request.getId());
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        }
        if (!request.getId().equals(HandleCurrentUserSession.getCurrentUser().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "User not authorized to do this operation"));
        }

        String imagePath = FilesManagement.upload(request.getImage(), request.getId(), "user-profile");
        if (imagePath == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Upload failed"));
        }

        user.get().setProfileImagePath(imagePath);
        userRepository.save(user.get());

        return ResponseEntity.ok(Map.of("message", "Profile image uploaded", "imageUrl", imagePath));
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('User')")
    public ResponseEntity<?> logUserProgressInProgram(UserProgressRequest userProgressRequest) {
        var user = HandleCurrentUserSession.getCurrentUser();
        var programWorkout = programWorkoutRepository.findById(userProgressRequest.getProgram_workout_id()).orElse(null);
        if (programWorkout == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "program_workout not found"));
        UserProgress userProgress = new UserProgress();
        userProgress.setUser(user);
        userProgress.setProgramWorkout(programWorkout);
        userProgress.setRecordedAt(LocalDate.now());
        if (userProgressRequest.getWeight() != null)
            userProgress.setWeight(userProgressRequest.getWeight());
        if (userProgressRequest.getDuration() != null)
            userProgress.setDuration(userProgressRequest.getDuration());
        if (userProgressRequest.getNote() != null)
            userProgress.setNote(userProgressRequest.getNote());
        userProgressRepository.save(userProgress);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "progress recorded successfully"));
    }

    @PreAuthorize("hasAnyAuthority('User')")
    public ResponseEntity<?> getUserProgressInProgram(UserProgressRequest userProgressRequest) {
        var user = HandleCurrentUserSession.getCurrentUser();
        var programWorkout = programWorkoutRepository.findById(userProgressRequest.getProgram_workout_id()).orElse(null);
        if (programWorkout == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "program_workout not found"));
        LocalDate startDate = userProgressRequest.getStartDate();
        LocalDate endDate = userProgressRequest.getEndDate();
        if (startDate == null || endDate == null) {
            endDate = LocalDate.now();
            startDate = endDate.minusDays(30);
        }
        var userProgress = userProgressRepository.findByUserIdAndProgramWorkoutIdAndRecordedAtBetween(
                user.getId(),
                programWorkout.getId(),
                startDate,
                endDate
        );
        return ResponseEntity.ok(buildProgressResponse(userProgress));
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('User')")
    public ResponseEntity<?> deleteRecodedProgressInProgram(Integer userProgressId) {
        var userProgress = userProgressRepository.findById(userProgressId).orElse(null);
        if (userProgress == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "user progress not found"));
        userProgressRepository.deleteById(userProgress.getId());
        userProgressRepository.flush();
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "recorded progress deleted"));
    }

    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
    public ResponseEntity<?> getUserProgressDashboard(UserProgressRequest userProgressRequest) {
        var user = userRepository.findById(userProgressRequest.getUserId()).orElse(null);
        var program = programRepository.findById(userProgressRequest.getProgramId()).orElse(null);
        var workout = workoutRepository.findById(userProgressRequest.getWorkoutId()).orElse(null);
        if (user == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "user not found"));
        if (program == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "program not found"));
        if (workout == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "workout not found"));
        var programWorkout = programWorkoutRepository.findByProgramIdAndWorkoutId(program.getId(), workout.getId()).orElse(null);
        if (programWorkout == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "program workout not found"));

        LocalDate startDate = userProgressRequest.getStartDate();
        LocalDate endDate = userProgressRequest.getEndDate();
        if (startDate == null || endDate == null) {
            endDate = LocalDate.now();
            startDate = endDate.minusDays(30);
        }
        var userProgress = userProgressRepository.findByUserIdAndProgramWorkoutIdAndRecordedAtBetween(
                user.getId(),
                programWorkout.getId(),
                startDate,
                endDate
        );
        return ResponseEntity.ok(buildProgressResponse(userProgress));
    }

    private List<UserProgressResponse> buildProgressResponse(List<UserProgress> userProgress) {
        List<UserProgressResponse> response = new ArrayList<>();
        for (UserProgress item : userProgress) {
            UserProgressResponse element = new UserProgressResponse();
            element.setId(item.getId());
            element.setWeight(item.getWeight());
            element.setDuration(item.getDuration());
            element.setRecordedAt(item.getRecordedAt());
            element.setNote(item.getNote());
            response.add(element);
        }
        return response;
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('User')")
    public ResponseEntity<?> logHealthInfo(HealthInfoRequest healthInfoRequest) {
        var user = HandleCurrentUserSession.getCurrentUser();
        var previousInfo = healthInfoRepository.findTopByUserIdOrderByRecordedAtDesc(user.getId()).orElse(null);

        if (previousInfo == null)
            return logHealthInfoFirstTime(healthInfoRequest);

        var newHealthInfo = new HealthInfo();

        newHealthInfo.setUser(user);
        newHealthInfo.setRecordedAt(LocalDate.now());

        // Use previous values if current request is null
        newHealthInfo.setHeightCm(
                healthInfoRequest.getHeightCm() != null ? healthInfoRequest.getHeightCm() : previousInfo.getHeightCm()
        );
        newHealthInfo.setWeightKg(
                healthInfoRequest.getWeightKg() != null ? healthInfoRequest.getWeightKg() : previousInfo.getWeightKg()
        );
        newHealthInfo.setArmCircumference(
                healthInfoRequest.getArmCircumference() != null ? healthInfoRequest.getArmCircumference() : previousInfo.getArmCircumference()
        );
        newHealthInfo.setThighCircumference(
                healthInfoRequest.getThighCircumference() != null ? healthInfoRequest.getThighCircumference() : previousInfo.getThighCircumference()
        );
        newHealthInfo.setWaistCircumference(
                healthInfoRequest.getWaistCircumference() != null ? healthInfoRequest.getWaistCircumference() : previousInfo.getWaistCircumference()
        );
        if (healthInfoRequest.getNotes() != null)
            newHealthInfo.setNotes(healthInfoRequest.getNotes());

        healthInfoRepository.save(newHealthInfo);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "health info recorded successfully"));
    }

    @Transactional
    private ResponseEntity<?> logHealthInfoFirstTime(HealthInfoRequest healthInfoRequest) {
        var newHealthInfo = new HealthInfo();
        if (healthInfoRequest.getHeightCm() == null || healthInfoRequest.getWeightKg() == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "height and weight must not be null for the first time"));
        newHealthInfo.setHeightCm(healthInfoRequest.getHeightCm());
        newHealthInfo.setWeightKg(healthInfoRequest.getWeightKg());
        if (healthInfoRequest.getArmCircumference() != null)
            newHealthInfo.setArmCircumference(healthInfoRequest.getArmCircumference());
        if (healthInfoRequest.getThighCircumference() != null)
            newHealthInfo.setThighCircumference(healthInfoRequest.getThighCircumference());
        if (healthInfoRequest.getWaistCircumference() != null)
            newHealthInfo.setWaistCircumference(healthInfoRequest.getWaistCircumference());
        if (healthInfoRequest.getNotes() != null)
            newHealthInfo.setNotes(healthInfoRequest.getNotes());
        newHealthInfo.setRecordedAt(LocalDate.now());
        newHealthInfo.setUser(HandleCurrentUserSession.getCurrentUser());
        healthInfoRepository.save(newHealthInfo);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "health info recorded successfully"));
    }

    public ResponseEntity<?> getUserHealthInfo(HealthInfoRequest healthInfoRequest) {
        var user = userRepository.findById(healthInfoRequest.getUserId()).orElse(null);
        if (user == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "user not found"));
        LocalDate startDate = healthInfoRequest.getStartDate();
        LocalDate endDate = healthInfoRequest.getEndDate();
        if (startDate == null || endDate == null) {
            endDate = LocalDate.now();
            startDate = endDate.minusDays(30);
        }
        var userHealthInfo = healthInfoRepository.findByUserIdAndRecordedAtBetween(
                user.getId(),
                startDate,
                endDate
        );
        return ResponseEntity.ok(buildHealthInfoResponse(userHealthInfo));
    }

    private List<HealthInfoResponse> buildHealthInfoResponse(List<HealthInfo> healthInfoList) {
        List<HealthInfoResponse> response = new ArrayList<>();
        for (HealthInfo item : healthInfoList) {
            HealthInfoResponse element = new HealthInfoResponse();
            element.setId(item.getId());
            element.setHeightCm(item.getHeightCm());
            element.setWeightKg(item.getWeightKg());
            element.setArmCircumference(item.getArmCircumference());
            element.setWaistCircumference(item.getWaistCircumference());
            element.setThighCircumference(item.getThighCircumference());
            element.setBMI(BMI_Calculator.calculateBMI(item.getWeightKg(), item.getHeightCm()));
            element.setStatus(BMI_Calculator.calculateBMIStatus(item.getWeightKg(), item.getHeightCm()));
            element.setRecordedAt(item.getRecordedAt());
            element.setNotes(item.getNotes());
            response.add(element);
        }
        return response;
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('User')")
    public ResponseEntity<?> deleteHealthInfo(Integer healthInfoId) {
        var userHealthInfo = healthInfoRepository.findById(healthInfoId).orElse(null);
        if (userHealthInfo == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "health info found"));
        healthInfoRepository.deleteById(userHealthInfo.getId());
        healthInfoRepository.flush();
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "recorded health info deleted"));
    }

    public ResponseEntity<?> saveUserFcmToken(String token) {
        var user = HandleCurrentUserSession.getCurrentUser();
        if (token == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "FCM is null"));
        user.setFcmToken(token);
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "FCM token saved successfully"));
    }

}
