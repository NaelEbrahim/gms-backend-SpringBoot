package com.graduation.GMS.DTO.Response;

import com.fasterxml.jackson.annotation.JsonInclude;
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
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<MealResponse> breakfast;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<MealResponse> lunch;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<MealResponse> dinner;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<MealResponse> snack;

}
