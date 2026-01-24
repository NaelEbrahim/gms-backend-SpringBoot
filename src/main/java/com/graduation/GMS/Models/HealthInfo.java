package com.graduation.GMS.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HealthInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private LocalDate recordedAt;

    private Float weightKg;

    private Float heightCm;

    private Float ArmCircumference;

    private Float ThighCircumference;

    private Float WaistCircumference;

    private Float improvementPercentage;

    private String notes;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
