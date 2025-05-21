package com.graduation.GMS.DTO.Request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssignProgramToUserRequest {
    private Integer programId;
    private Integer userId;
}
