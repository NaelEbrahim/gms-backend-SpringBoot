package com.graduation.GMS.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

    @NotBlank(message = "Primary muscle is required")
    @Size(max = 50, message = "Primary muscle must not exceed 50 characters")
    private String primaryMuscle;

    @Size(max = 50, message = "Secondary muscles must not exceed 50 characters")
    private String secondaryMuscles;

    private Float avgCalories;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
}
