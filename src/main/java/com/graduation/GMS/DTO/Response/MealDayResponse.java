package com.graduation.GMS.DTO.Response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("Breakfast")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<MealResponse> Breakfast;

    @JsonProperty("Lunch")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<MealResponse> Lunch;

    @JsonProperty("Dinner")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<MealResponse> Dinner;

    @JsonProperty("Snack")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<MealResponse> Snack;
}
