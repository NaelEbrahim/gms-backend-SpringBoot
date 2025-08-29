package com.graduation.GMS.Services;

import com.graduation.GMS.DTO.Request.*;
import com.graduation.GMS.DTO.Response.*;
import com.graduation.GMS.Models.*;
import com.graduation.GMS.Models.DietPlan;
import com.graduation.GMS.Models.Enums.Day;
import com.graduation.GMS.Models.Enums.MealTime;
import com.graduation.GMS.Models.Enums.Roles;
import com.graduation.GMS.Repositories.*;
import com.graduation.GMS.Handlers.HandleCurrentUserSession;
import com.graduation.GMS.Services.GeneralServices.NotificationService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.graduation.GMS.DTO.Response.UserResponse.mapToUserResponse;

@Service
@AllArgsConstructor
public class DietService {

    private DietPlanRepository dietPlanRepository;

    private Plan_MealRepository planMealRepository;

    private MealRepository mealRepository;

    private UserRepository userRepository;

    private User_DietRepository userDietRepository;

    private NotificationService notificationService;

    private NotificationRepository notificationRepository;

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
    public ResponseEntity<?> createDiet(DietRequest request) {
        // Check if the diet title already exists (optional validation)
        Optional<DietPlan> existingDietPlan = dietPlanRepository.findByTitle(request.getTitle());
        if (existingDietPlan.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Diet Plan title already exists"));
        }
        // Convert the DTO to entity and save
        DietPlan dietPlanEntity = new DietPlan();

        Optional<User> userOptional = userRepository.findById(request.getCoachId());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        }

        dietPlanEntity.setTitle(request.getTitle());
        dietPlanEntity.setCoach(userOptional.get());
        dietPlanEntity.setCreatedAt(LocalDateTime.now());
        dietPlanEntity.setLastModifiedAt(LocalDateTime.now());
        // Save the dietPlan to the database
        dietPlanRepository.save(dietPlanEntity);


        // Create and send notification
        Notification notification = new Notification();
        notification.setTitle("New Diet Plan");
        notification.setContent("New Diet Plan has been made :" + dietPlanEntity.getTitle());
        notification.setCreatedAt(LocalDateTime.now());
        // Persist notification first
        notification = notificationRepository.save(notification); // Save and get managed instance

        List<User> usersWithUserRole = userRepository.findAllByRoleName(Roles.User);

        notificationService.sendNotificationToUsers(
                usersWithUserRole,
                notification
        );

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

        Optional<User> userOptional = userRepository.findById(request.getCoachId());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        }
        if(!existingDietPlan.getCoach().getId().equals(request.getCoachId())) {
            existingDietPlan.setCoach(userOptional.get());
        }

        if (!existingDietPlan.getTitle().equals(request.getTitle()) && !request.getTitle().isEmpty()) {
            existingDietPlan.setTitle(request.getTitle());
        }

        existingDietPlan.setLastModifiedAt(LocalDateTime.now());
        dietPlanRepository.save(existingDietPlan);

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Diet Plan updated successfully"));
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
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

        DietPlan dietPlan = dietPlanOptional.get();

        // Build the schedule
        ScheduleResponse schedule = buildScheduleResponse(dietPlan);

        // Get feedbacks
        List<UserFeedBackResponse> feedBacks = userDietRepository.findFeedbackByDietPlan(dietPlan)
                .stream()
                .map(ud -> new UserFeedBackResponse(ud.getUser(), ud.getFeedBack()))
                .toList();

        DietResponse response = new DietResponse(
                dietPlan.getId(),
                mapToUserResponse(dietPlan.getCoach()),
                dietPlan.getTitle(),
                dietPlan.getCreatedAt(),
                dietPlan.getLastModifiedAt(),
                calculateRate(dietPlan.getId()),
                schedule,
                feedBacks
        );

        return ResponseEntity.ok(response);
    }

    private ScheduleResponse buildScheduleResponse(DietPlan dietPlan) {
        Map<Day, MealDayResponse> schedule = new LinkedHashMap<>();

        planMealRepository.findByDietPlan(dietPlan)
                .stream()
                .collect(Collectors.groupingBy(Plan_Meal::getDay))
                .forEach((day, dayMeals) -> {
                    Map<MealTime, List<MealResponse>> mealsByTime = dayMeals.stream()
                            .collect(Collectors.groupingBy(
                                    Plan_Meal::getMealTime,
                                    Collectors.mapping(pm -> new MealResponse(
                                            pm.getMeal().getId(),
                                            pm.getMeal().getTitle(),
                                            pm.getMeal().getImagePath(),
                                            pm.getMeal().getCalories(),
                                            pm.getQuantity(),
                                            pm.getMeal().getDescription(),
                                            calculateCalories(pm.getMeal().getCalories(), pm.getQuantity())
                                    ), Collectors.toList())
                            ));

                    schedule.put(day, new MealDayResponse(
                            mealsByTime.getOrDefault(MealTime.Breakfast, List.of()),
                            mealsByTime.getOrDefault(MealTime.Lunch, List.of()),
                            mealsByTime.getOrDefault(MealTime.Dinner, List.of()),
                            mealsByTime.getOrDefault(MealTime.Snack, List.of())
                    ));
                });

        return new ScheduleResponse(schedule);
    }

    public Float calculateCalories(Float baseCalories, Float quantity) {
        if (baseCalories == null || quantity == null) return 0.0f;
        return (baseCalories / 100) * quantity;
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

                    return new DietResponse(
                            dietPlan.getId(),
                            mapToUserResponse(dietPlan.getCoach()),
                            dietPlan.getTitle(),
                            dietPlan.getCreatedAt(),
                            dietPlan.getLastModifiedAt(),
                            calculateRate(dietPlan.getId()),
                            buildScheduleResponse(dietPlan),
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
        boolean exists = planMealRepository.existsByDietPlanAndMealAndDayAndMealTime(dietPlanEntity, mealEntity, request.getDay(), request.getMealTime());
        if (exists) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Meal is already assigned to this dietPlan"));
        }

        // Create and save the relationship
        Plan_Meal planMeal = new Plan_Meal();
        planMeal.setDietPlan(dietPlanEntity);
        planMeal.setMeal(mealEntity);
        planMeal.setQuantity(request.getQuantity());
        planMeal.setDay(request.getDay());
        planMeal.setMealTime(request.getMealTime());

        planMealRepository.save(planMeal);

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Meal successfully assigned to dietPlan"));
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
    public ResponseEntity<?> updateAssignedMealToDiet(AssignMealToDietRequest request) {
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


        Optional<Plan_Meal> planMealOpt = planMealRepository.findByDietPlanAndMealAndDayAndMealTime(dietPlanEntity,
                mealEntity, request.getDay(), request.getMealTime());

        if (planMealOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Meal not assigned to diet plan"));
        }

        Plan_Meal pm = planMealOpt.get();

        if (request.getQuantity() != null && !request.getQuantity().equals(pm.getQuantity())) {
            pm.setQuantity(request.getQuantity());
        }

        planMealRepository.save(pm);

        return ResponseEntity.ok(Map.of("message", "Meal assignment updated successfully"));
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
    public ResponseEntity<?> unAssignMealFromDiet(AssignMealToDietRequest request) {
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

        Optional<Plan_Meal> planMealOpt = planMealRepository.findByDietPlanAndMealAndDayAndMealTime(dietPlanEntity,
                mealEntity, request.getDay(), request.getMealTime());

        if (planMealOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Meal not assigned to diet plan"));
        }

        planMealRepository.delete(planMealOpt.get());
        return ResponseEntity.ok(Map.of("message", "Meal successfully removed from diet plan"));
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

        // Create and send notification
        Notification notification = new Notification();
        notification.setTitle("New Assignment");
        notification.setContent("New Diet Plan has been assigned to You:" + dietPlan.getTitle());
        notification.setCreatedAt(LocalDateTime.now());
        // Persist notification first
        notification = notificationRepository.save(notification); // Save and get managed instance

        notificationService.sendNotification(
                userOptional.get(),
                notification
        );

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

        // Create and send notification
        Notification notification = new Notification();
        notification.setTitle("New Assignment");
        notification.setContent("Diet Plan has been Un assigned to You:" + dietPlan.getTitle());
        notification.setCreatedAt(LocalDateTime.now());
        // Persist notification first
        notification = notificationRepository.save(notification); // Save and get managed instance

        notificationService.sendNotification(
                userOptional.get(),
                notification
        );

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
                    return new UserFeedBackResponse(user, User_Diet.getFeedBack());
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

                    // Create a list with ONLY the current user's feedback
                    List<UserFeedBackResponse> userFeedbackList = new ArrayList<>();
                    if (ud.getFeedBack() != null && !ud.getFeedBack().trim().isEmpty()) {
                        userFeedbackList.add(new UserFeedBackResponse(currentUser, ud.getFeedBack()));
                    }

                    return new DietResponse(
                            dietPlan.getId(),
                            mapToUserResponse(dietPlan.getCoach()),
                            dietPlan.getTitle(),
                            dietPlan.getCreatedAt(),
                            dietPlan.getLastModifiedAt(),
                            ud.getRate(),
                            buildScheduleResponse(dietPlan),
                            userFeedbackList
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
                                        meal.getImagePath(),
                                        meal.getCalories(),
                                        pm.getQuantity(),
                                        meal.getDescription(),
                                        calculateCalories(meal.getCalories(), pm.getQuantity())
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
                            buildScheduleResponse(dietPlan),
                            null
                    );
                })
                .toList();

        return ResponseEntity.ok(dietResponses);
    }
}
