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
public class UserFeedBackResponse extends UserResponse {
    private String feedback;

    public UserFeedBackResponse(User user, String feedback) {
        super(  user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail());
        this.feedback = feedback;
    }
}
