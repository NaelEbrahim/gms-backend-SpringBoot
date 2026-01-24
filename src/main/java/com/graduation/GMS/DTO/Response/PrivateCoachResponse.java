package com.graduation.GMS.DTO.Response;

import com.graduation.GMS.Models.User;
import lombok.Getter;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;

@Getter
public class PrivateCoachResponse {

    private final UserResponse coach;

    private final Boolean isActive;

    private final LocalDate startedAt;

    private final Float rate;

    public PrivateCoachResponse(User coach, LocalDate startedAt, Float rate) {
        this.coach = UserResponse.mapToUserResponse(coach);
        this.isActive =  !LocalDate.now().isAfter(startedAt.plusDays(30));
        this.startedAt = startedAt;
        this.rate = rate;
    }
}
