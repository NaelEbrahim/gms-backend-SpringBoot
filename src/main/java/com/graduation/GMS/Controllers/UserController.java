package com.graduation.GMS.Controllers;

import com.graduation.GMS.DTO.Request.*;
import com.graduation.GMS.Models.Enums.Roles;
import com.graduation.GMS.Services.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api/user")
@AllArgsConstructor
public class UserController {

    private final UserService userService;


    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        return userService.userLogin(loginRequest);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestBody Map<String, String> body) {
        return userService.refreshAccessToken(body.get("refreshToken"));
    }

    @PostMapping("/createUser")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserRequest userRequest) {
        return userService.createUser(userRequest);
    }

    @PutMapping("upload-profile-image")
    public ResponseEntity<?> uploadUserProfileImage(@ModelAttribute ImageRequest request) {
        return userService.uploadUserProfileImage(request);
    }

    @PutMapping("/updateProfile/{userId}")
    public ResponseEntity<?> updateUserProfile(@PathVariable Integer userId, @Valid @RequestBody UpdateProfileRequest profileRequest) {
        return userService.updateProfile(userId, profileRequest);
    }

    @PutMapping("/resetPassword")
    public ResponseEntity<?> resetUserPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        return userService.resetPassword(resetPasswordRequest);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> userLogout() {
        return userService.logout();
    }

    @GetMapping("/by-role")
    public ResponseEntity<?> getUsersByRole(
            @RequestParam Roles role,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page == null || size == null) {
            return userService.getUsersByRole(role, null);
        }
        Pageable pageable = PageRequest.of(page, size);
        if (role.equals(Roles.All)) {
            return userService.getAll(pageable);
        } else {
            return userService.getUsersByRole(role, pageable);
        }
    }

    // -- Private Coach Assignment APIs --

    @PostMapping("/assignCoach")
    public ResponseEntity<?> assignCoachToUser(@Valid @RequestBody AssignPrivateCoachToUserRequest request) {
        return userService.assignPrivateCoachToUser(request);
    }

    @PostMapping("/updateAssignCoach")
    public ResponseEntity<?> updateAssignCoachToUser(@Valid @RequestBody AssignPrivateCoachToUserRequest request) {
        return userService.updateAssignPrivateCoachToUser(request);
    }

    @PostMapping("/unassignCoach")
    public ResponseEntity<?> unAssignCoachToUser(@Valid @RequestBody UnAssignPrivateCoachToUserRequest request) {
        return userService.unAssignCoachToUser(request);
    }

    @PostMapping("/add-private-coach-rate")
    public ResponseEntity<?> addCoachRate(@RequestBody Map<String, Integer> data) {
        return userService.addCoachRate(data.get("coachId"), data.get("rate"));
    }

    // --- Attendance APIs ---

    @PostMapping("/attendance")
    public ResponseEntity<?> createAttendanceFromQr(@Valid @RequestBody QrAttendanceRequest request) {
        return userService.createAttendanceFromQr(request);
    }

    @GetMapping("/attendance/{userId}")
    public ResponseEntity<?> getUserAttendanceById(@PathVariable Integer userId) {
        return userService.getUserAttendanceById(userId);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> userProfile() {
        return userService.getUserProfile();
    }


    @PostMapping("/logProgress")
    public ResponseEntity<?> addProgressInProgram(@Valid @RequestBody UserProgressRequest userProgressRequest) {
        return userService.logUserProgressInProgram(userProgressRequest);
    }

    @GetMapping("/getProgressByRange")
    public ResponseEntity<?> getProgressInProgramByRange(@Valid @RequestBody UserProgressRequest userProgressRequest) {
        return userService.getUserProgressInProgram(userProgressRequest);
    }

    @DeleteMapping("/deleteProgress/{progressId}")
    public ResponseEntity<?> deleteProgressInProgram(@PathVariable Integer progressId) {
        return userService.deleteRecodedProgressInProgram(progressId);
    }

    @GetMapping("/getUserProgressDashboard")
    public ResponseEntity<?> getUserProgressForDashboard(@Valid @RequestBody UserProgressRequest userProgressRequest) {
        return userService.getUserProgressDashboard(userProgressRequest);
    }

    @PostMapping("/saveFcmToken")
    public ResponseEntity<?> saveFcmToken(@RequestBody Map<String, String> body) {
        return userService.saveUserFcmToken(body.get("fcmToken"));
    }

    @PostMapping("/forgotPassword")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgetPasswordRequest request) {
        return userService.forgotPassword(request);
    }

    @PostMapping("/verifyResetCode")
    public ResponseEntity<?> verifyResetCode(@Valid @RequestBody ForgetPasswordRequest request) {
        return userService.verifyResetCode(request);
    }

    @PutMapping("/resetForgotPassword")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ForgetPasswordRequest request) {
        return userService.resetForgotPassword(request);
    }

    @GetMapping("/getUserPrivateCoaches/{userId}")
    public ResponseEntity<?> getPrivateCoaches(@PathVariable Integer userId) {
        return userService.getUserCoaches(userId);
    }

    @PostMapping("/logHealthInfo")
    public ResponseEntity<?> logHealthInfo(@Valid @RequestBody HealthInfoRequest request) {
        return userService.logHealthInfo(request);
    }

    @GetMapping("/getHealthInfoByRange")
    public ResponseEntity<?> getHealthInfoHistory(@Valid @RequestBody HealthInfoRequest request) {
        return userService.getUserHealthInfo(request);
    }

    @DeleteMapping("/deleteHealthInfo/{healthInfoId}")
    public ResponseEntity<?> deleteUserHealthInfo(@PathVariable Integer healthInfoId) {
        return userService.deleteHealthInfo(healthInfoId);
    }

}
