package com.graduation.GMS.Controllers;

import com.graduation.GMS.DTO.Request.*;
import com.graduation.GMS.Models.Enums.Roles;
import com.graduation.GMS.Services.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
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
    public ResponseEntity<?> uploadUserProfileImage(@ModelAttribute ImageRequest request){
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
    public ResponseEntity<?> getUsersByRole(@RequestParam String role) {
        if (role.equalsIgnoreCase("Admin")) {
            return userService.getUsersByRole(Roles.Admin);
        } else if (role.equalsIgnoreCase("Secretary")) {
            return userService.getUsersByRole(Roles.Secretary);
        } else if (role.equalsIgnoreCase("User")) {
            return userService.getUsersByRole(Roles.User);
        } else if (role.equalsIgnoreCase("Coach")) {
            return userService.getUsersByRole(Roles.Coach);
        } else
            return userService.getAll();
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

    @PostMapping("/healthInfo")
    public ResponseEntity<?> createOrUpdateHealthInfo(@Valid @RequestBody HealthInfoRequest request) {
        return userService.createOrUpdateHealthInfo(request);
    }

    @GetMapping("/healthInfo/history/{userId}")
    public ResponseEntity<?> getHealthInfoHistory(@PathVariable Integer userId) {
        return userService.getHealthInfoHistory(userId);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> userProfile() {
        return userService.getUserProfile();
    }

    @GetMapping("/qr")
    public ResponseEntity<?> userQR(){
        return userService.getUserQR();
    }

}
