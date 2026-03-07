package com.graduation.GMS.Handlers;

import com.graduation.GMS.Models.User;
import com.graduation.GMS.Repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class HandleCurrentUserSession {

    private static UserRepository userRepository;

    public HandleCurrentUserSession(UserRepository userRepository) {
        HandleCurrentUserSession.userRepository = userRepository;
    }

    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Integer userId)) {
            throw new UsernameNotFoundException("User not authenticated");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));
    }

}
