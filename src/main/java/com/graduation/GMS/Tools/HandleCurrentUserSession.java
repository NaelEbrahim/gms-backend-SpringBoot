package com.graduation.GMS.Tools;

import com.graduation.GMS.Models.User;
import com.graduation.GMS.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class HandleCurrentUserSession {

    private static UserRepository userRepository;

    @Autowired
    public HandleCurrentUserSession(UserRepository userRepository) {
        HandleCurrentUserSession.userRepository = userRepository;
    }

    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = (String) authentication.getPrincipal(); // it's a String
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
