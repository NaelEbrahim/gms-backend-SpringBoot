package com.graduation.GMS.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MealDayResponse {
    private List<MealResponse> breakfast;

    private List<MealResponse> lunch;

    private List<MealResponse> dinner;

    private List<MealResponse> snack;

}
