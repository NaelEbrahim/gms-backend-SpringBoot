package com.graduation.GMS.DTO.Response;

import com.graduation.GMS.Models.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantResponse extends UserResponse {
    private Float score;

    public ParticipantResponse(User user, Float score) {
        super(user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getGender(),
                user.getDob(),
                user.getCreatedAt(),
                user.getQr());
        this.score = score;
    }
}

