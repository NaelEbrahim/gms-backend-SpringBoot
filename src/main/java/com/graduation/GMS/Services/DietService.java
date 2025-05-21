package com.graduation.GMS.Services;

import com.graduation.GMS.DTO.Request.*;
import com.graduation.GMS.DTO.Response.*;
import com.graduation.GMS.Models.*;
import com.graduation.GMS.Models.DietPlan;
import com.graduation.GMS.Repositories.*;
import com.graduation.GMS.Tools.HandleCurrentUserSession;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    private User_DietRepository userDietRepository;

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
    public ResponseEntity<?> createDiet(DietRequest request) {
        // Check if the diet title already exists (optional validation)
        Optional<DietPlan> existingDietPlan = dietPlanRepository.findByTitle(request.getTitle());
        if (existingDietPlan.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Diet Plan title already exists"));
        }
        // Convert the DTO to entity and save
        DietPlan dietPlanEntity = new DietPlan();

        dietPlanEntity.setTitle(request.getTitle());
        dietPlanEntity.setCoach(HandleCurrentUserSession.getCurrentUser());
        dietPlanEntity.setCreatedAt(LocalDateTime.now());
        dietPlanEntity.setLastModifiedAt(LocalDateTime.now());
        // Save the dietPlan to the database
        dietPlanRepository.save(dietPlanEntity);
        // Return the response with the saved dietPlan details
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Diet Plan created successfully"));
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
    public ResponseEntity<?> updateDiet(Integer id, DietRequest request) {
        Optional<DietPlan> optionalDietPlan = dietPlanRepository.findById(id);
        if (optionalDietPlan.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Diet Plan not found"));
        }

        DietPlan existingDietPlan = optionalDietPlan.get();


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
                calculateRate(dietPlanEntity.getId()),
                mealResponses,
                null
        );

        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    private Float calculateRate(int dietId) {
        // First check if the diet exists
        Optional<DietPlan> dietPlanOptional = dietPlanRepository.findById(dietId);
        if (dietPlanOptional.isEmpty()) {
            return 0.0F;
        }

        // Get all User_Diet entries for this program
        List<User_Diet> userDiets = userDietRepository.findByDietPlan(dietPlanOptional.get());

        if (userDiets.isEmpty()) {
            return 0.0F;
        }

        // Calculate average rating using stream API
        Double average = userDiets.stream()
                .filter(up -> up.getRate() != null)  // Filter out null ratings
                .mapToDouble(User_Diet::getRate)  // Convert to double for calculation
                .average()                           // Calculate average
                .orElse(0.0);                        // Default to 0.0 if no ratings

        return average.floatValue();  // Convert back to Float
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
                            calculateRate(dietPlan.getId()),
                            mealResponses,
                            null
                    );
                })
                .toList();

        return ResponseEntity.status(HttpStatus.OK).body(dietPlanResponses);
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
    public ResponseEntity<?> assignMealToDiet(AssignMealToDietRequest request) {
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

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
    public ResponseEntity<?> assignDietToUser(AssignDietToUser request) {
        Optional<User> userOptional = userRepository.findById(request.getUserId());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        }

        Optional<DietPlan> dietPlanOptional = dietPlanRepository.findById(request.getDietId());
        if (dietPlanOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Diet Plan not found"));
        }

        DietPlan dietPlan = dietPlanOptional.get();
        User user = userOptional.get();

        // Check if already assigned
        boolean exists = userDietRepository.existsByUserAndDietPlan(user, dietPlan);
        if (exists) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Diet Plan is already assigned to this user"));
        }

        // Create and save the relationship
        User_Diet userDiet = new User_Diet();
        userDiet.setDiet_plan(dietPlan);
        userDiet.setUser(user);
        userDiet.setStartedAt(LocalDateTime.now());
        userDietRepository.save(userDiet);

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Diet Plan successfully assigned to user"));
    }
    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
    public ResponseEntity<?> unAssignDietToUser(AssignDietToUser request) {
        Optional<User> userOptional = userRepository.findById(request.getUserId());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        }

        Optional<DietPlan> dietPlanOptional = dietPlanRepository.findById(request.getDietId());
        if (dietPlanOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Diet Plan not found"));
        }

        DietPlan dietPlan = dietPlanOptional.get();
        User user = userOptional.get();

        // Find the existing assignment
        Optional<User_Diet> userProgramOptional = userDietRepository.findByUserAndDietPlan(user, dietPlan);
        if (userProgramOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Diet Plan is not assigned to this user"));
        }

        // Delete the assignment
        userDietRepository.delete(userProgramOptional.get());

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Diet Plan successfully unassigned from user"));
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('User')")
    public ResponseEntity<?> rateDietPlan(RateDietRequest request) {

        User user = HandleCurrentUserSession.getCurrentUser();

        // Validate diet exists
        Optional<DietPlan> dietPlanOptional = dietPlanRepository.findById(request.getDietId());
        if (dietPlanOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Diet Plan not found"));
        }

        DietPlan dietPlan = dietPlanOptional.get();

        // Check if diet is assigned to user
        if (!userDietRepository.existsByUserAndDietPlan(user, dietPlan)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You can only rate diets assigned to you"));
        }

        // Validate rating (1-5)
        if (request.getRate() == null || request.getRate() < 1 || request.getRate() > 5) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Rating must be between 1 and 5"));
        }

        // Find or create user-diet relationship
        User_Diet userDiet = userDietRepository.findByUserAndDietPlan(user, dietPlan)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        // Update rating
        userDiet.setRate(request.getRate());
        userDietRepository.save(userDiet);


        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Diet Plan rated successfully"));
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('User')")
    public ResponseEntity<?> submitFeedback(FeedBackDietRequest request) {

        User user = HandleCurrentUserSession.getCurrentUser();

        // Validate diet exists
        Optional<DietPlan> dietPlanOptional = dietPlanRepository.findById(request.getDietId());
        if (dietPlanOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Diet Plan not found"));
        }

        DietPlan dietPlan = dietPlanOptional.get();

        // Check if diet is assigned to user
        if (!userDietRepository.existsByUserAndDietPlan(user, dietPlan)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You can only submit feedback for diets assigned to you"));
        }

        // Validate feedback not empty
        if (request.getFeedback() == null || request.getFeedback().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Feedback cannot be empty"));
        }

        // Find user-diet relationship
        User_Diet userDiet = userDietRepository.findByUserAndDietPlan(user, dietPlan)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        // Update feedback
        userDiet.setFeedBack(request.getFeedback());
        userDietRepository.save(userDiet);

        return ResponseEntity.ok(Map.of("message", "Feedback submitted successfully"));
    }

    public ResponseEntity<?> getAllDietPlanFeedBacks(Integer dietId) {
        Optional<DietPlan> dietPlanOptional = dietPlanRepository.findById(dietId);
        if (dietPlanOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Diet Plan not found"));
        }

        DietPlan dietPlan = dietPlanOptional.get();

        List<UserFeedBackResponse> feedBacks = userDietRepository.findFeedbackByDietPlan(dietPlan)
                .stream()
                .map(User_Diet -> {
                    User user = User_Diet.getUser();
                    return new UserFeedBackResponse(user,User_Diet.getFeedBack());
                })
                .toList();

        DietResponse responseDto = new DietResponse(
                dietPlan.getId(),
                mapToUserResponse(dietPlan.getCoach()),
                dietPlan.getTitle(),
                dietPlan.getCreatedAt(),
                dietPlan.getLastModifiedAt(),
                calculateRate(dietPlan.getId()),
                null,
                feedBacks
        );
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }
    @PreAuthorize("hasAnyAuthority('User')")
    public ResponseEntity<?> getMyAssignedDiets() {
        User currentUser = HandleCurrentUserSession.getCurrentUser();

        List<User_Diet> userDiets = userDietRepository.findByUser(currentUser);

        if (userDiets.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No diets assigned to the current user"));
        }

        List<DietResponse> dietResponses = userDiets.stream()
                .map(ud -> {
                    DietPlan dietPlan = ud.getDiet_plan();

                    List<MealResponse> meals = planMealRepository.findByDietPlan(dietPlan)
                            .stream()
                            .map(pm -> {
                                Meal meal = pm.getMeal();
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
                            calculateRate(dietPlan.getId()),
                            meals,
                            null
                    );
                })
                .toList();

        return ResponseEntity.ok(dietResponses);
    }

    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
    public ResponseEntity<?> getAssignedDietsByUserId(Integer userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        }

        User user = userOptional.get();
        List<User_Diet> userDiets = userDietRepository.findByUser(user);

        if (userDiets.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No diets assigned to this user"));
        }

        List<DietResponse> dietResponses = userDiets.stream()
                .map(ud -> {
                    DietPlan dietPlan = ud.getDiet_plan();

                    List<MealResponse> meals = planMealRepository.findByDietPlan(dietPlan)
                            .stream()
                            .map(pm -> {
                                Meal meal = pm.getMeal();
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
                            calculateRate(dietPlan.getId()),
                            meals,
                            null
                    );
                })
                .toList();

        return ResponseEntity.ok(dietResponses);
    }
}
