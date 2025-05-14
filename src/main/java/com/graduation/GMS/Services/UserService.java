package com.graduation.GMS.Services;

import com.graduation.GMS.Config.SecurityConfig;
import com.graduation.GMS.DTO.Request.UserRequest;
import com.graduation.GMS.DTO.Request.LoginRequest;
import com.graduation.GMS.DTO.Response.UserResponse;
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

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Secretary')")
    public ResponseEntity<?> createUser(UserRequest createRequest) throws Exception {
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
        newUser.setFirstName(createRequest.getFirstName());
        newUser.setLastName(createRequest.getLastName());
        newUser.setEmail(createRequest.getEmail());
        //Test BEGIN
        String pass = Generators.generatePassword();
        System.out.println(pass);
        //Test END
        newUser.setPassword(securityConfig.passwordEncoder().encode(pass));
        newUser.setGender(createRequest.getGender());
        newUser.setDob(createRequest.getDob());
        newUser.setPhoneNumber(createRequest.getPhoneNumber());
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setQr(Generators.generateQRCode(createRequest.getEmail()));
        var savedUser = userRepository.save(newUser);
        //Roles
        for (Roles roleName : createRequest.getRoles()) {
            Role role = roleRepository.findByRoleName(roleName)
                    .orElseGet(() -> roleRepository.save(Role.builder().roleName(roleName).build()));
            userRoleRepository.save(User_Role.builder()
                    .user(savedUser)
                    .role(role)
                    .build());
        }
        UserResponse userResponse = new UserResponse(savedUser, null);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userResponse);
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
            authTokenRepository.deleteByUserId(user.getId());
            authTokenRepository.flush();
            String accessToken = jwtService.generateAccessToken(user, userRoles);
            authTokenRepository.save(AuthToken.builder().user(user).accessToken(accessToken).build());
            String refreshToken = jwtService.generateRefreshToken(user);
            var cookie = new Cookie("refreshToken", refreshToken);
            cookie.setSecure(true);
            cookie.setHttpOnly(true);
            cookie.setMaxAge(259200);
            cookie.setPath("/auth/refresh");
            response.addCookie(cookie);
            UserResponse userResponse = new UserResponse(user, accessToken);
            return ResponseEntity.ok().body(Map.of("message", userResponse));
        } else
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "invalid username or password"));
    }


}
