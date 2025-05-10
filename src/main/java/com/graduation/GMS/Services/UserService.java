package com.graduation.GMS.Services;

import com.graduation.GMS.Config.SecurityConfig;
import com.graduation.GMS.DTO.Request.CreateUserRequest;
import com.graduation.GMS.DTO.Request.LoginRequest;
import com.graduation.GMS.DTO.Response.CreateUserResponse;
import com.graduation.GMS.Models.AuthToken;
import com.graduation.GMS.Models.User;
import com.graduation.GMS.Repositories.AuthTokenRepository;
import com.graduation.GMS.Repositories.UserRepository;
import com.graduation.GMS.Tools.Generators;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final AuthTokenRepository authTokenRepository;

    private final SecurityConfig securityConfig;

    private final JwtService jwtService;


    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Secretary')")
    public ResponseEntity<?> createUser(CreateUserRequest createRequest) throws Exception {
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
        //String pass = Generators.generatePassword();
        newUser.setPassword(securityConfig.passwordEncoder().encode(Generators.generatePassword()));
        newUser.setGender(createRequest.getGender());
        newUser.setDob(createRequest.getDob());
        newUser.setPhoneNumber(createRequest.getPhoneNumber());
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setQr(Generators.generateQRCode(createRequest.getEmail()));
        // Tokens
        AuthToken userTokens = new AuthToken();
        userTokens.setUser(newUser);
        System.out.println(createRequest.getRoles());
        userTokens.setAccessToken(jwtService.generateJwt(newUser, createRequest.getRoles()));
        CreateUserResponse userResponse = new CreateUserResponse(userRepository.save(newUser), authTokenRepository.save(userTokens));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userResponse);
    }

    public ResponseEntity<?> userLogin(LoginRequest loginRequest) {
        var user = userRepository.findByEmail(loginRequest.getEmail()).orElse(null);
        if (user != null && securityConfig.passwordEncoder().matches(loginRequest.getPassword(), user.getPassword())) {
            // TODO : generate token
            return ResponseEntity.ok().body(Map.of("message:", user));
        } else
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message:", "invalid username or password"));
    }


}
