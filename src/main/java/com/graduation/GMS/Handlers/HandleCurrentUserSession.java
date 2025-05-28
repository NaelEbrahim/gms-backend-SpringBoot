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
        String email = (String) authentication.getPrincipal();// it's a String
        System.out.println(email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

}
