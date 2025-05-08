package com.graduation.GMS.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MealRequest {

    @NotBlank(message = "Program title is required")
    @Size(max = 100, message = "Program title must not exceed 100 characters")
    private String title;

    private String calories;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
}
