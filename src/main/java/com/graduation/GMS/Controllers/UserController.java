package com.graduation.GMS.Controllers;

import com.graduation.GMS.DTO.Request.*;
import com.graduation.GMS.Models.Enums.Roles;
import com.graduation.GMS.Services.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/user")
@AllArgsConstructor
public class UserController {

    private final UserService userService;


    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        return userService.userLogin(loginRequest, response);
    }

    @PostMapping("/createUser")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserRequest userRequest) throws Exception {
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
            @RequestParam String role,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        if (role.equalsIgnoreCase("Admin")) {
            return userService.getUsersByRoleWithSearch(Roles.Admin, keyword, pageable);
        } else if (role.equalsIgnoreCase("Secretary")) {
            return userService.getUsersByRoleWithSearch(Roles.Secretary, keyword, pageable);
        } else if (role.equalsIgnoreCase("User")) {
            return userService.getUsersByRoleWithSearch(Roles.User, keyword, pageable);
        } else if (role.equalsIgnoreCase("Coach")) {
            return userService.getUsersByRoleWithSearch(Roles.Coach, keyword, pageable);
        } else
            return userService.getUsersByRoleWithSearch(Roles.User, keyword, pageable);
    }

    // --- Private Coach Assignment APIs ---

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

    // --- Attendance APIs ---

    @PostMapping("/attendance")
    public ResponseEntity<?> createAttendanceFromQr(@Valid @RequestBody QrAttendanceRequest request) {
        return userService.createAttendanceFromQr(request);
    }

    @GetMapping("/attendance/{userId}")
    public ResponseEntity<?> getUserAttendanceById(@PathVariable Integer userId) {
        return userService.getUserAttendanceById(userId);
    }

    // --- Health Info APIs ---

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

    @GetMapping("/profile")
    public ResponseEntity<?> userProfile() {
        return userService.getUserProfile();
    }

    @GetMapping("/qr")
    public ResponseEntity<?> userQR() {
        return userService.getUserQR();
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

}
