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
public class UserWithPasswordResponse extends ProfileResponse{
    private String password;

    public UserWithPasswordResponse(User user, String password) {
        super(user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getProfileImagePath(),
                user.getPhoneNumber(),
                user.getGender(),
                user.getDob(),
                user.getCreatedAt(),
                null,
                null
        );
        this.password = password;
    }
}
