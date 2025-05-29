package com.graduation.GMS.Models;

import com.graduation.GMS.Models.Enums.Day;
import com.graduation.GMS.Models.Enums.MealTime;
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

    @Column
    private Float quantity;

    @Column
    @Enumerated(EnumType.STRING)
    private Day day;

    @Column
    @Enumerated(EnumType.STRING)
    private MealTime mealTime;

    @ManyToOne
    @JoinColumn(name = "meal_id")
    private Meal meal;

    @ManyToOne
    @JoinColumn(name = "diet_plan_id")
    private DietPlan dietPlan;


}