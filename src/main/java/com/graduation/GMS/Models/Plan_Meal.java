package com.graduation.GMS.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Plan_Meal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "meal_id")
    private Meal meal;

    @ManyToOne
    @JoinColumn(name = "diet_plan_id")
    private DietPlan diet_plan;

}