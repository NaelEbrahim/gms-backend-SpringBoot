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
    private Integer mealId;

    private Integer dietId;

    private Float quantity;

    private Day day;

    private MealTime mealTime;

}
