package com.graduation.GMS.DTO.Request;

import com.graduation.GMS.Models.Enums.Muscle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateWorkoutRequest {
    @NotBlank(message = "Workout title is required")
    private String title;

    private Muscle primaryMuscle;

    private Muscle secondaryMuscles;

    private Float avgCalories;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private MultipartFile image;

}
