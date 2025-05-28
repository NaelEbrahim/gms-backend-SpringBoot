package com.graduation.GMS.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MealResponse {

    private Integer id;

    private String title;

    private Float baseCalories;  // per 100g

    private Float quantity;      // in grams

    private String description;

    private Float totalCalories; // calculated field

}
