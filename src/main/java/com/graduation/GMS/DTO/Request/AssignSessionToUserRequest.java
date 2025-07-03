package com.graduation.GMS.DTO.Request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssignSessionToUserRequest {
    private Integer sessionId;
    private Integer userId;
}
