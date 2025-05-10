package com.graduation.GMS.Services;

import com.graduation.GMS.DTO.Request.MealRequest;
import com.graduation.GMS.DTO.Response.MealResponse;
import com.graduation.GMS.Models.Meal;
import com.graduation.GMS.Repositories.MealRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class MealService {

    private MealRepository mealRepository;

    @Transactional
    public ResponseEntity<?> createMeal(@Valid MealRequest request) {
        // Check if meal title already exists
        Optional<Meal> existingMeal = mealRepository.findByTitle(request.getTitle());
        if (existingMeal.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Meal title already exists"));
        }

        // Create and save new meal
        Meal meal = new Meal();
        meal.setTitle(request.getTitle());
        meal.setDescription(request.getDescription());
        meal.setCalories(request.getCalories());

        mealRepository.save(meal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Meal created successfully"));
    }

    @Transactional
    public ResponseEntity<?> updateMeal(Integer id, @Valid MealRequest request) {
        Optional<Meal> optionalMeal = mealRepository.findById(id);
        if (optionalMeal.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Meal not found"));
        }
        Meal meal = optionalMeal.get();


        if (!request.getTitle().isEmpty() && !meal.getTitle().equals(request.getTitle())) {
            meal.setTitle(request.getTitle());
        }

        if (!request.getDescription().isEmpty() && !meal.getDescription().equals(request.getDescription())) {
            meal.setDescription(request.getDescription());
        }

        if (!request.getCalories().isEmpty() && !meal.getCalories().equals(request.getCalories())) {
            meal.setCalories(request.getCalories());
        }

        mealRepository.save(meal);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Meal updated successfully"));
    }

    public ResponseEntity<?> deleteMeal(Integer id) {
        if (!mealRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Meal not found"));
        }

        mealRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Meal deleted successfully"));
    }

    public ResponseEntity<?> getMealById(Integer id) {
        Optional<Meal> mealOptional = mealRepository.findById(id);
        if (mealOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Meal not found"));
        }

        Meal meal = mealOptional.get();
        MealResponse response = new MealResponse(
                meal.getId(),
                meal.getTitle(),
                meal.getDescription(),
                meal.getCalories()
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    public ResponseEntity<?> getAllMeals() {
        List<Meal> meals = mealRepository.findAll();

        if (meals.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No meals found"));
        }

        List<MealResponse> mealResponses = meals.stream()
                .map(w -> new MealResponse(
                        w.getId(),
                        w.getTitle(),
                        w.getDescription(),
                        w.getCalories()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK)
                .body(mealResponses);
    }
}
