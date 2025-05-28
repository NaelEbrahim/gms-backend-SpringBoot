package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.DietPlan;
import com.graduation.GMS.Models.Enums.Day;
import com.graduation.GMS.Models.Enums.MealTime;
import com.graduation.GMS.Models.Meal;
import com.graduation.GMS.Models.Plan_Meal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface Plan_MealRepository extends JpaRepository<Plan_Meal, Integer> {
    boolean existsByDietPlanAndMeal(DietPlan dietPlanEntity, Meal mealEntity);

    List<Plan_Meal> findByDietPlan(DietPlan dietPlanEntity);

    Optional<Plan_Meal> findByDietPlanAndMeal(DietPlan dietPlan, Meal meal);

    Optional<Plan_Meal> findByDietPlanAndMealAndDayAndMealTime(DietPlan dietPlanEntity, Meal mealEntity, Day day, MealTime mealTime);

    boolean existsByDietPlanAndMealAndDayAndMealTime(DietPlan dietPlanEntity, Meal mealEntity ,Day day, MealTime mealTime);
}
