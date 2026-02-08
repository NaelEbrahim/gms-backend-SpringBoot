package com.graduation.GMS.Services;


import com.graduation.GMS.Config.SecurityConfig;
import com.graduation.GMS.DTO.Request.*;
import com.graduation.GMS.DTO.Response.*;
import com.graduation.GMS.Handlers.HandleCurrentUserSession;
import com.graduation.GMS.Models.*;
import com.graduation.GMS.Models.Enums.Roles;
import com.graduation.GMS.Repositories.*;
import com.graduation.GMS.Services.GeneralServices.JwtService;
import com.graduation.GMS.Services.GeneralServices.NotificationService;
import com.graduation.GMS.Services.GeneralServices.VerificationCodeService;
import com.graduation.GMS.Tools.FilesManagement;
import com.graduation.GMS.Tools.Generators;
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
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

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

    private final VerificationCodeService verificationCodeService;

    private final SubscriptionHistoryRepository subscriptionHistoryRepository;


    @PreAuthorize("hasAnyAuthority('Admin','Secretary')")
    public ResponseEntity<?> createUser(UserRequest createRequest) {
        return internalCreateUser(createRequest);
    }

    @Transactional
    public ResponseEntity<?> internalCreateUser(UserRequest createRequest) {
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
    public ResponseEntity<?> userLogin(LoginRequest loginRequest) {
        var user = userRepository.findByEmail(loginRequest.getEmail()).orElse(null);
        if (user != null && securityConfig.passwordEncoder().matches(loginRequest.getPassword(), user.getPassword())) {
            //Fetch User Roles
            var ur = userRoleRepository.findByUserId(user.getId());
            List<Roles> userRoles = new ArrayList<>();
            for (User_Role element : ur)
                userRoles.add(element.getRole().getRoleName());
            // Tokens
            invalidateUserToken(user);
            String accessToken = jwtService.generateAccessToken(user, userRoles);
            String refreshToken = jwtService.generateRefreshToken(user);
            authTokenRepository.save(AuthToken.builder().user(user).accessToken(accessToken).refreshToken(refreshToken).build());
            ProfileResponse userResponse = new ProfileResponse(user, refreshToken, accessToken);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(Map.of("message", userResponse));
        } else
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "invalid username or password"));
    }

    boolean checkSubscription(User user) {
        boolean isExpired = false;
        List<SubscriptionHistory> latestSubs = subscriptionHistoryRepository.findLatestSubscriptions();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Damascus"));
        for (SubscriptionHistory sh : latestSubs) {
            LocalDate endDate = sh.getPaymentDate().toLocalDate().plusMonths(1);
            if (!endDate.isAfter(today)) {
                isExpired = true;
                break;
            }
        }
        return isExpired;
    }

    @Transactional
    public ResponseEntity<?> refreshAccessToken(String refreshToken) {
        if (refreshToken != null && authTokenRepository.findByRefreshToken(refreshToken).isPresent() && jwtService.validateToken(refreshToken)) {
            try {
                String userId = jwtService.extractId(refreshToken);
                var user = userRepository.findById(Integer.parseInt(userId)).orElse(null);
                if (user == null)
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid refresh token"));
                //Fetch User Roles
                var ur = userRoleRepository.findByUserId(user.getId());
                List<Roles> userRoles = new ArrayList<>();
                for (User_Role element : ur)
                    userRoles.add(element.getRole().getRoleName());
                invalidateUserToken(user);
                // Generate new access & refresh token
                String newAccessToken = jwtService.generateAccessToken(user, userRoles);
                String newRefreshToken = jwtService.generateRefreshToken(user);
                authTokenRepository.save(AuthToken.builder().user(user).accessToken(newAccessToken).refreshToken(newRefreshToken).build());
                Map<String, Object> response = new HashMap<>();
                response.put("accessToken", newAccessToken);
                response.put("refreshToken", newRefreshToken);
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid Refresh token"));
        }
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
            else
                user.setEmail(userRequest.getEmail());
        }
        if (userRequest.getPhoneNumber() != null && !userRequest.getPhoneNumber().equals(user.getPhoneNumber())) {
            if (userRepository.findByPhoneNumber(userRequest.getPhoneNumber()).isPresent())
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message:", "phone number already exist"));
            else
                user.setPhoneNumber(userRequest.getPhoneNumber());
        }
        if (userRequest.getDob() != null && !userRequest.getDob().equals(user.getDob())) {
            user.setDob(userRequest.getDob());
        }
        if (userRequest.getGender() != null && !userRequest.getGender().equals(user.getGender())) {
            user.setGender(userRequest.getGender());
        }
        var savedUser = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", UserResponse.mapToUserResponse(savedUser)));
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
            invalidateUserToken(user);
            SecurityContextHolder.clearContext();
            return ResponseEntity.status(HttpStatus.OK)
                    .body(Map.of("message", "logout successfully"));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "something went wrong"));
    }

    @Transactional
    public void invalidateUserToken(User user) {
        user.setFcmToken(null);
        authTokenRepository.deleteByUserId(user.getId());
        authTokenRepository.flush();
    }

    public ResponseEntity<?> getUsersByRole(Roles roleName, Pageable pageable) {
        Page<User> usersPage = userRepository.findUsersByRole(roleName, pageable);
        List<ProfileResponse> responses = usersPage
                .stream()
                .map(ProfileResponse::mapToProfileResponse)
                .toList();
        Map<String, Object> result = new HashMap<>();
        result.put("role", roleName);
        result.put("count", usersPage.getTotalElements());
        result.put("totalPages", usersPage.getTotalPages());
        result.put("currentPage", usersPage.getNumber());
        result.put("users", responses);
        return ResponseEntity.ok(Map.of("message", result));
    }

    public ResponseEntity<?> getAll(Pageable pageable) {
        Page<User> usersPage = userRepository.findAllUsers(pageable);
        List<ProfileResponse> responses = usersPage
                .stream()
                .map(ProfileResponse::mapToProfileResponse)
                .toList();
        Map<String, Object> result = new HashMap<>();
        result.put("count", usersPage.getTotalElements());
        result.put("totalPages", usersPage.getTotalPages());
        result.put("currentPage", usersPage.getNumber());
        result.put("users", responses);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", result));
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
        //userCoach.setStartedAt(LocalDateTime.now());
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

        //userCoach.setStartedAt(LocalDateTime.now());
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
    public ResponseEntity<?> createAttendanceFromQr(int userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "User not found for the QR code"));
        }

        // Check if attendance for today already exists
        LocalDate today = LocalDate.now();
        boolean attendanceExists = attendanceRepository.existsByUserAndDate(user, today);
        if (attendanceExists) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Attendance for today is already recorded"));
        }

        Attendance attendance = new Attendance();
        attendance.setUser(user);
        attendance.setDate(today);
        attendanceRepository.save(attendance);

        Notification notification = new Notification();
        notification.setTitle("Attendance Notification");
        notification.setContent("Thank you for your attendance");
        notification.setCreatedAt(LocalDateTime.now());
        notification = notificationRepository.save(notification);

        notificationService.sendNotification(user, notification);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Attendance recorded successfully"));
    }


    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Secretary')")
    public ResponseEntity<?> getUserAttendanceByRange(Integer userId, LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Start date cannot be after end date"));
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "User not found"));
        }

        // Fetch attendance for user within range
        List<Attendance> attendanceList = attendanceRepository.findByUserAndDateBetween(user, start, end);

        List<LocalDate> attendanceDates = attendanceList.stream()
                .map(Attendance::getDate)
                .toList();

        return ResponseEntity.ok(Map.of("message", attendanceDates));
    }


    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Secretary')")
    public ResponseEntity<?> getAllAttendanceByRange(LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Start date cannot be after end date"));
        }

        // Fetch all attendance within range
        List<Attendance> attendanceList = attendanceRepository.findByDateBetween(start, end);

        // Group by user
        Map<String, List<LocalDate>> grouped = attendanceList.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getUser().getFirstName() + " " + a.getUser().getLastName(),
                        Collectors.mapping(Attendance::getDate, Collectors.toList())
                ));

        return ResponseEntity.ok(Map.of("message", grouped));
    }


    public ResponseEntity<?> getUserProfile() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", ProfileResponse.mapToProfileResponse(HandleCurrentUserSession.getCurrentUser())));
    }

    @Transactional
    public ResponseEntity<?> uploadUserProfileImage(ImageRequest request) {
        var user = userRepository.findById(request.getId()).orElse(null);
        if (user == null) {
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
        user.setProfileImagePath(imagePath);
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", Map.of("profileImagePath", imagePath)));
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
        var user = userRepository.findById(userProgressRequest.getUserId()).orElse(null);
        var programWorkout = programWorkoutRepository.findById(userProgressRequest.getProgram_workout_id()).orElse(null);
        if (user == null || programWorkout == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "user or program_workout id not found"));
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

    public ResponseEntity<?> forgotPassword(ForgetPasswordRequest request) {
        var user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "email not found"));
        }
        verificationCodeService.sendVerificationCode(request.getEmail(), user.getFirstName(), user.getLastName());
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "verification code sent to your email"));
    }

    public ResponseEntity<?> verifyResetCode(ForgetPasswordRequest request) {
        boolean isValid = verificationCodeService.verifyCode(request.getEmail(), request.getCode());
        if (isValid) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(Map.of("message", "code verified"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "invalid code"));
        }
    }

    public ResponseEntity<?> resetForgotPassword(ForgetPasswordRequest request) {
        var user = userRepository.findByEmail(request.getEmail());
        if (user.isPresent() && request.getNewPassword() != null) {
            user.get().setPassword(securityConfig.passwordEncoder().encode(request.getNewPassword()));
            userRepository.save(user.get());
            verificationCodeService.clearCode(request.getEmail());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(Map.of("message", "password reset successfully"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "user not found or new password is invalid"));
    }

    public ResponseEntity<?> getUserCoaches(Integer userId) {
        var user = userRepository.findById(userId).orElse(null);
        if (user == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "user not found"));
        List<PrivateCoachResponse> userCoaches = new ArrayList<>();
        for (PrivateCoach element : privateCoachRepository.findByUserId(userId))
            userCoaches.add(new PrivateCoachResponse(element.getCoach(), element.getStartedAt(), element.getUserRate()));
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", userCoaches));
    }

    public ResponseEntity<?> addCoachRate(Integer coachId, Integer rate) {
        var user = HandleCurrentUserSession.getCurrentUser();
        var coach = userRepository.findById(coachId).orElse(null);
        var privateCoach = privateCoachRepository.findByUserAndCoach(user, coach).orElse(null);
        if (privateCoach == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "coach not assign to this user"));
        }
        privateCoach.setUserRate((float) rate);
        privateCoachRepository.save(privateCoach);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "rate updated"));
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
    public ResponseEntity<?> logHealthInfoFirstTime(HealthInfoRequest healthInfoRequest) {
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "user not found"));
        LocalDate startDate = healthInfoRequest.getStartDate();
        LocalDate endDate = healthInfoRequest.getEndDate();
        if (startDate == null || endDate == null) {
            endDate = healthInfoRepository.findTopByUserIdOrderByRecordedAtDesc(user.getId())
                    .map(HealthInfo::getRecordedAt)
                    .orElse(LocalDate.now());
            startDate = endDate.minusDays(30); // last month from last recorded date
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "health info found"));
        healthInfoRepository.deleteById(userHealthInfo.getId());
        healthInfoRepository.flush();
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "recorded health info deleted"));
    }

    @PreAuthorize("hasAnyAuthority('Admin','Coach','Secretary')")
    public ResponseEntity<?> getCoachUsers(int coachId) {
        User coach = userRepository.findById(coachId).orElse(null);
        if (coach == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "user not found"));
        }
        List<PrivateCoachResponse> coachUsers = new ArrayList<>();
        for (PrivateCoach element : privateCoachRepository.findByCoachId(coachId))
            coachUsers.add(new PrivateCoachResponse(element.getUser(), element.getStartedAt(), element.getUserRate()));
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", coachUsers));
    }

}