package com.graduation.GMS.DTO.Request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HealthInfoRequest {

    @NotNull(message = "User ID must not be null")
    private Integer userId;

    @NotNull(message = "Weight must not be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Weight must be greater than 0")
    private Float weightKg;

    @NotNull(message = "Height must not be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Height must be greater than 0")
    private Float heightCm;

    @DecimalMin(value = "0.0", message = "Improvement percentage must be at least 0")
    @DecimalMax(value = "100.0", message = "Improvement percentage cannot exceed 100")
    private Float improvementPercentage;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}
