package com.graduation.GMS.Config;

import com.graduation.GMS.DTO.Request.UserRequest;
import com.graduation.GMS.Models.Enums.Gender;
import com.graduation.GMS.Models.Enums.Roles;
import com.graduation.GMS.Repositories.UserRepository;
import com.graduation.GMS.Services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserService userService;

    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            UserRequest admin = new UserRequest();
            admin.setFirstName("Ahmad");
            admin.setLastName("Hallak");
            admin.setEmail("ahmedHallak457@gmail.com");
            admin.setPhoneNumber("0945621467");
            admin.setGender(Gender.Male);
            admin.setDob(LocalDate.of(1997,4,16));
            admin.setPassword("e&X7a*gT");
            List<Roles> roles = new ArrayList<>();
            roles.add(Roles.Admin);
            roles.add(Roles.Coach);
            admin.setRoles(roles);
            userService.createUser(admin);
        }
    }

}
