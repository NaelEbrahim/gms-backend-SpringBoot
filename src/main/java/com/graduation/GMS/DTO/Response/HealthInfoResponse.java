package com.graduation.GMS.DTO.Response;

import com.fasterxml.jackson.annotation.JsonFormat;

import com.graduation.GMS.Models.HealthInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HealthInfoResponse {

    private Integer id;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recordedAt;

    private Float weightKg;

    private Float heightCm;

    private Float improvementPercentage;

    private String notes;

    public static HealthInfoResponse fromEntity(HealthInfo healthInfo) {
        return new HealthInfoResponse(
                healthInfo.getId(),
                healthInfo.getRecordedAt(),
                healthInfo.getWeightKg(),
                healthInfo.getHeightCm(),
                healthInfo.getImprovementPercentage(),
                healthInfo.getNotes());
    }
}
