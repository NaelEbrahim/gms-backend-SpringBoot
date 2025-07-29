package com.graduation.GMS.Models;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate recordedAt;

    private Float weightKg;

    private Float heightCm;

    private Float waistCircumference;

    private Float armCircumference;

    private Float thighCircumference;

    private String notes;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
