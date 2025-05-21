package com.graduation.GMS.DTO.Request;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssignDietToUser {
    private Integer dietId;
    private Integer userId;
}
