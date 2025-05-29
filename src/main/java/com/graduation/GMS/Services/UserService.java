package com.graduation.GMS.Services;

import com.graduation.GMS.Config.SecurityConfig;
import com.graduation.GMS.DTO.Request.ResetPasswordRequest;
import com.graduation.GMS.DTO.Request.UpdateProfileRequest;
import com.graduation.GMS.DTO.Request.UserRequest;
import com.graduation.GMS.DTO.Request.LoginRequest;
import com.graduation.GMS.DTO.Response.UserResponse;
import com.graduation.GMS.DTO.Response.UserWithPasswordResponse;
import com.graduation.GMS.Handlers.HandleCurrentUserSession;
import com.graduation.GMS.Models.AuthToken;
import com.graduation.GMS.Models.Enums.Roles;
import com.graduation.GMS.Models.Role;
import com.graduation.GMS.Models.User;
import com.graduation.GMS.Models.User_Role;
import com.graduation.GMS.Repositories.AuthTokenRepository;
import com.graduation.GMS.Repositories.RoleRepository;
import com.graduation.GMS.Repositories.UserRepository;
import com.graduation.GMS.Repositories.User_RoleRepository;
import com.graduation.GMS.Tools.Generators;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
            UserResponse userResponse = new UserResponse(user, null, accessToken);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(Map.of("message", userResponse));
        } else
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "invalid username or password"));
    }

    @Transactional
    public ResponseEntity<?> updateProfile(Integer userId, UpdateProfileRequest userRequest) {
        if (userRequest.getEmail() != null && userRepository.findByEmail(userRequest.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message:", "email already exist"));
        }
        if (userRequest.getPhoneNumber() != null && userRepository.findByPhoneNumber(userRequest.getPhoneNumber()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message:", "phone number already exist"));
        }
        var user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message:", "user not found"));
        }
        if (userRequest.getFirstName() != null) {
            user.setFirstName(userRequest.getFirstName());
        }
        if (userRequest.getLastName() != null) {
            user.setLastName(userRequest.getLastName());
        }
        if (userRequest.getEmail() != null) {
            user.setEmail(userRequest.getEmail());
            try {
                user.setQr(Generators.generateQRCode(userRequest.getEmail()));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("message:", e.getMessage()));
            }
        }
        if (userRequest.getPhoneNumber() != null) {
            user.setPhoneNumber(userRequest.getPhoneNumber());
        }
        if (userRequest.getDob() != null) {
            user.setDob(userRequest.getDob());
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


}
