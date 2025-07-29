package com.graduation.GMS.DTO.Response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HealthInfoResponse {
    private Integer id;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate recordedAt;

    private Float weightKg;

    private Float heightCm;

    private String notes;

    private Float waistCircumference;

    private Float armCircumference;

    private Float thighCircumference;

    private Float BMI;

    private String Status;

}
