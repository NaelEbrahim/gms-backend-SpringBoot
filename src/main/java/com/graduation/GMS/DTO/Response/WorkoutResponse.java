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
    private String primaryMuscle;
    private String secondaryMuscles;
    private String avgCalories;
    private String description;
}
