package com.graduation.GMS.DTO.Request;

import com.graduation.GMS.Models.Enums.Muscle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutRequest {

    @NotBlank(message = "Workout title is required")
    @Size(max = 100, message = "Workout title must not exceed 100 characters")
    private String title;

    private Muscle primaryMuscle;

    private Muscle secondaryMuscles;

    private Float avgCalories;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
}
