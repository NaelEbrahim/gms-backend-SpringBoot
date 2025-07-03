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
    //test

    private Integer id;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recordedAt;

    private Float weightKg;

    private Float heightCm;

    private Float improvementPercentage;

    private Float BMI;

    private String Status;

    private String notes;

    public static HealthInfoResponse fromEntity(HealthInfo healthInfo) {
        return new HealthInfoResponse(
                healthInfo.getId(),
                healthInfo.getRecordedAt(),
                healthInfo.getWeightKg(),
                healthInfo.getHeightCm(),
                healthInfo.getImprovementPercentage(),
                calculateBMI(healthInfo.getWeightKg(), healthInfo.getHeightCm()),
                getBMIStatus(healthInfo.getWeightKg(), healthInfo.getHeightCm()),
                healthInfo.getNotes());
    }

    static Float calculateBMI(Float weightKg, Float heightCm) {
        if (weightKg == 0.0f  && heightCm == 0.0f ) return 0.0f;
        return weightKg / ((heightCm / 100) * (heightCm / 100));
    }
    static String getBMIStatus(Float weightKg, Float heightCm) {
          float bmi = calculateBMI(weightKg, heightCm);
          if (bmi == 0.0f) return "--";
          if (bmi < 18.5f) return "Weight loss";
          if (bmi < 25f) return "normal Weight";
          if (bmi < 30f) return "Weight gain";
          return "Corpulence";
    }
}
