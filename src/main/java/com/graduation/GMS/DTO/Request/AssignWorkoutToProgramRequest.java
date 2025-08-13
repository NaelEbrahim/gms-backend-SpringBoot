package com.graduation.GMS.DTO.Request;

import com.graduation.GMS.Models.Enums.Day;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignWorkoutToProgramRequest {
    @NotNull(message = "Program ID is required")
    private Integer programId;

    @NotNull(message = "Workout ID is required")
    private Integer workoutId;

    private Day day;

    private Integer reps;

    private Integer sets;

    private Integer duration;
}