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
public class UserWithPasswordResponse extends UserResponse{
    private String password;

    public UserWithPasswordResponse(User user, String password) {
        super(user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getGender(),
                user.getDob(),
                user.getCreatedAt(),
                user.getQr());
        this.password = password;
    }
}
