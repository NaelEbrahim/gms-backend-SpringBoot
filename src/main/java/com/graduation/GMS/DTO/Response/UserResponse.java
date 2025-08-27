package com.graduation.GMS.DTO.Response;

import com.graduation.GMS.Models.User;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Integer id;

    private String firstName;

    private String lastName;

    private String email;

    private String profileImagePath;

    public UserResponse(Integer id, String firstName, String lastName, String email) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public static UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getProfileImagePath()
        );
    }

}
