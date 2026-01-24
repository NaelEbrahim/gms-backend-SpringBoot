package com.graduation.GMS.DTO.Request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HealthInfoRequest {

    private Integer userId;

    @DecimalMin(value = "0.0", inclusive = false, message = "Weight must be greater than 0")
    private Float weightKg;

    @DecimalMin(value = "0.0", inclusive = false, message = "Height must be greater than 0")
    private Float heightCm;

    @DecimalMin(value = "0.0", inclusive = false, message = "waist circumference must be greater than 0")
    private Float waistCircumference;

    @DecimalMin(value = "0.0", inclusive = false, message = "arm circumference must be greater than 0")
    private Float armCircumference;

    @DecimalMin(value = "0.0", inclusive = false, message = "thigh circumference must be greater than 0")
    private Float thighCircumference;

    private String notes;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
}