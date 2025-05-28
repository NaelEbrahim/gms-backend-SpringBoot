package com.graduation.GMS.DTO.Response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class WorkoutDayResponse {

    private List<WorkoutResponse> Chest;

    private List<WorkoutResponse> Back;

    private List<WorkoutResponse> Shoulders;

    private List<WorkoutResponse> Biceps;

    private List<WorkoutResponse> Triceps;

    private List<WorkoutResponse> Forearms;

    private List<WorkoutResponse> Abs;

    private List<WorkoutResponse> Glutes;

    private List<WorkoutResponse> Quadriceps;

    private List<WorkoutResponse> Hamstrings;

    private List<WorkoutResponse> Calves;

}
