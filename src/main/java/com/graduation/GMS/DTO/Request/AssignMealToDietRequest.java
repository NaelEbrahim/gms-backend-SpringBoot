package com.graduation.GMS.DTO.Request;

import com.graduation.GMS.Models.Enums.Day;
import com.graduation.GMS.Models.Enums.MealTime;
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

    private Float quantity;

    private Day day;

    private MealTime mealTime;

}
