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

    private String imagePath;

    private Integer reps;

    private Integer sets;

    private Integer program_workout_id;

    public WorkoutResponse(Integer id, String title, Float baseAvgCalories, String primaryMuscle, String secondaryMuscles, Float totalBurnedCalories, String description, String imagePath, Integer reps, Integer sets) {
        this.id = id;
        this.title = title;
        this.baseAvgCalories = baseAvgCalories;
        this.primaryMuscle = primaryMuscle;
        this.secondaryMuscles = secondaryMuscles;
        this.totalBurnedCalories = totalBurnedCalories;
        this.description = description;
        this.imagePath = imagePath;
        this.reps = reps;
        this.sets = sets;
    }

}
