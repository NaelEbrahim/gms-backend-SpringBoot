package com.graduation.GMS.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutResponse {

    private Integer id;

    private String title;

    private Float baseAvgCalories;

    private String primaryMuscle;

    private String secondaryMuscles;

    private Float totalBurnedCalories;

    private String description;

    private Integer reps;

    private Integer sets;
}
