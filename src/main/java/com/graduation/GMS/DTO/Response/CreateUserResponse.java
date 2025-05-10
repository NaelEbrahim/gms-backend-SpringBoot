package com.graduation.GMS.DTO.Response;

import com.graduation.GMS.Models.AuthToken;
import com.graduation.GMS.Models.User;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class CreateUserResponse {

    private User userInfo;

    private AuthToken userTokens;

}
