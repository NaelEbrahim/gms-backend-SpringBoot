package com.graduation.GMS.Services;

import com.graduation.GMS.DTO.Request.AssignMealToDietRequest;
import com.graduation.GMS.DTO.Request.DietRequest;
import com.graduation.GMS.DTO.Response.MealResponse;
import com.graduation.GMS.DTO.Response.DietResponse;
import com.graduation.GMS.DTO.Response.UserResponse;
import com.graduation.GMS.Models.*;
import com.graduation.GMS.Models.DietPlan;
import com.graduation.GMS.Repositories.DietPlanRepository;
import com.graduation.GMS.Repositories.MealRepository;
import com.graduation.GMS.Repositories.Plan_MealRepository;
import com.graduation.GMS.Repositories.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.graduation.GMS.DTO.Response.UserResponse.mapToUserResponse;

@Service
@AllArgsConstructor
public class DietService {

    private DietPlanRepository dietPlanRepository;

    private Plan_MealRepository planMealRepository;

    private MealRepository mealRepository;

    private UserRepository userRepository;

    @Transactional
    public ResponseEntity<?> createDiet(@Valid DietRequest request) {
        // Check if the diet title already exists (optional validation)
        Optional<DietPlan> existingDietPlan = dietPlanRepository.findByTitle(request.getTitle());
        if (existingDietPlan.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Diet Plan title already exists"));
        }
        // Convert the DTO to entity and save
        DietPlan dietPlanEntity = new DietPlan();
        Optional<User> coach =userRepository.findById(request.getCoachId());
        if (coach.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Coach Not Found"));
        }
        dietPlanEntity.setTitle(request.getTitle());
        dietPlanEntity.setCoach(coach.get());
        dietPlanEntity.setCreatedAt(LocalDateTime.now());
        dietPlanEntity.setLastModifiedAt(LocalDateTime.now());
        // Save the dietPlan to the database
        dietPlanRepository.save(dietPlanEntity);
        // Return the response with the saved dietPlan details
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Diet Plan created successfully"));
    }

    public ResponseEntity<?> updateDiet(Integer id, @Valid DietRequest request) {
        Optional<DietPlan> optionalDietPlan = dietPlanRepository.findById(id);
        if (optionalDietPlan.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Diet Plan not found"));
        }

        DietPlan existingDietPlan = optionalDietPlan.get();

        Optional<User> coach =userRepository.findById(request.getCoachId());
        if (coach.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Coach Not Found"));
        }

        if (!existingDietPlan.getCoach().getId().equals(request.getCoachId())) {
            existingDietPlan.setCoach(coach.get());
        }

        if (!existingDietPlan.getTitle().equals(request.getTitle())&&!request.getTitle().isEmpty()) {
            existingDietPlan.setTitle(request.getTitle());
        }

        existingDietPlan.setLastModifiedAt(LocalDateTime.now());
        dietPlanRepository.save(existingDietPlan);

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Diet Plan updated successfully"));
    }

    public ResponseEntity<?> deleteDiet(Integer id) {
        if (!dietPlanRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Diet Plan not found"));
        }

        dietPlanRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Diet Plan deleted successfully"));
    }

    public ResponseEntity<?> getDietById(Integer id) {
        Optional<DietPlan> dietPlanOptional = dietPlanRepository.findById(id);
        if (dietPlanOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Diet Plan not found"));
        }

        DietPlan dietPlanEntity = dietPlanOptional.get();
        UserResponse coachResponse = mapToUserResponse(dietPlanEntity.getCoach());
        // Get all meals associated with this diet plan
        List<MealResponse> mealResponses = planMealRepository.findByDietPlan(dietPlanEntity)
                .stream()
                .map(planMeal -> {
                    Meal meal = planMeal.getMeal();
                    return new MealResponse(
                            meal.getId(),
                            meal.getTitle(),
                            meal.getCalories(),
                            meal.getDescription()
                    );
                })
                .toList();

        DietResponse responseDto = new DietResponse(
                dietPlanEntity.getId(),
                coachResponse,
                dietPlanEntity.getTitle(),
                dietPlanEntity.getCreatedAt(),
                dietPlanEntity.getLastModifiedAt(),
                mealResponses
        );

        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    public ResponseEntity<?> getAllDiets() {
        List<DietPlan> dietPlans = dietPlanRepository.findAll();

        if (dietPlans.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No diet plans found"));
        }

        List<DietResponse> dietPlanResponses = dietPlans.stream()
                .map(dietPlan -> {
                    // Get meals for each diet plan
                    List<MealResponse> mealResponses = planMealRepository.findByDietPlan(dietPlan)
                            .stream()
                            .map(planMeal -> {
                                Meal meal = planMeal.getMeal();
                                return new MealResponse(
                                        meal.getId(),
                                        meal.getTitle(),
                                        meal.getCalories(),
                                        meal.getDescription()
                                );
                            })
                            .toList();

                    return new DietResponse(
                            dietPlan.getId(),
                            mapToUserResponse(dietPlan.getCoach()),
                            dietPlan.getTitle(),
                            dietPlan.getCreatedAt(),
                            dietPlan.getLastModifiedAt(),
                            mealResponses
                    );
                })
                .toList();

        return ResponseEntity.status(HttpStatus.OK).body(dietPlanResponses);
    }

    public ResponseEntity<?> assignMealToDiet(@Valid AssignMealToDietRequest request) {
        Integer dietPlanId = request.getDiet_plan_id();
        Integer mealId = request.getMeal_id();

        Optional<DietPlan> dietPlanOptional = dietPlanRepository.findById(dietPlanId);
        if (dietPlanOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Diet Plan not found"));
        }

        Optional<Meal> mealOptional = mealRepository.findById(mealId);
        if (mealOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Meal not found"));
        }

        DietPlan dietPlanEntity = dietPlanOptional.get();
        Meal mealEntity = mealOptional.get();

        // Optional: Check if already assigned
        boolean exists = planMealRepository.existsByDietPlanAndMeal(dietPlanEntity, mealEntity);
        if (exists) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Meal is already assigned to this dietPlan"));
        }

        // Create and save the relationship
        Plan_Meal planMeal = new Plan_Meal();
        planMeal.setDietPlan(dietPlanEntity);
        planMeal.setMeal(mealEntity);

        planMealRepository.save(planMeal);

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Meal successfully assigned to dietPlan"));
    }
}
