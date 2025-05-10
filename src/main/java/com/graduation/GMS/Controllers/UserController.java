package com.graduation.GMS.Controllers;

import com.graduation.GMS.DTO.Request.CreateUserRequest;
import com.graduation.GMS.DTO.Request.LoginRequest;
import com.graduation.GMS.Services.UserService;
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
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        return userService.userLogin(loginRequest);
    }

    @PostMapping("/createUser")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest createUserRequest) throws Exception {
        return userService.createUser(createUserRequest);
    }

}
