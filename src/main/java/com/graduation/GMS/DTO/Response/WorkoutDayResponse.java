package com.graduation.GMS.DTO.Response;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY) //
public class WorkoutDayResponse {

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<WorkoutResponse> Chest;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<WorkoutResponse> Back;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<WorkoutResponse> Shoulders;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<WorkoutResponse> Biceps;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<WorkoutResponse> Triceps;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<WorkoutResponse> Forearms;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<WorkoutResponse> Abs;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<WorkoutResponse> Glutes;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<WorkoutResponse> Quadriceps;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<WorkoutResponse> Hamstrings;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<WorkoutResponse> Calves;

}
