package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.DietPlan;
import com.graduation.GMS.Models.Meal;
import com.graduation.GMS.Models.Plan_Meal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface Plan_MealRepository extends JpaRepository<Plan_Meal, Integer> {
    boolean existsByDietPlanAndMeal(DietPlan dietPlanEntity, Meal mealEntity);


    List<Plan_Meal> findByDietPlan(DietPlan dietPlanEntity);
}
