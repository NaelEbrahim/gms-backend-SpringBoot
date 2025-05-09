package com.graduation.GMS.DTO.Request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssignMealToDietRequest {
    private Integer meal_id;
    private Integer diet_plan_id;
}
