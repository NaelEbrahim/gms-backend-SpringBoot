package com.graduation.GMS.Services;

import com.graduation.GMS.DTO.Request.CreateMealRequest;
import com.graduation.GMS.DTO.Request.ImageRequest;
import com.graduation.GMS.DTO.Request.MealRequest;
import com.graduation.GMS.DTO.Response.MealResponse;
import com.graduation.GMS.Models.Meal;
import com.graduation.GMS.Repositories.MealRepository;
import com.graduation.GMS.Tools.FilesManagement;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@AllArgsConstructor
public class MealService {

    private MealRepository mealRepository;

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
    public ResponseEntity<?> createMeal(CreateMealRequest request) {
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

//        String imagePath = FilesManagement.upload(request.getImage(), meal.getId(), "meals");
//        if (imagePath == null) {
//            throw new TransactionException("Upload failed") {
//            };
//        }
//        meal.setImagePath(imagePath);
//        mealRepository.save(meal);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Meal created successfully"));
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
    public ResponseEntity<?> updateMeal(Integer id, MealRequest request) {
        Meal meal = mealRepository.findById(id).orElse(null);
        if (meal == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Meal not found"));
        }

        if (!request.getTitle().isEmpty() && !meal.getTitle().equals(request.getTitle())) {
            meal.setTitle(request.getTitle());
        }

        if (!request.getDescription().isEmpty() && !meal.getDescription().equals(request.getDescription())) {
            meal.setDescription(request.getDescription());
        }

        if (request.getCalories() != null && !meal.getCalories().equals(request.getCalories())) {
            meal.setCalories(request.getCalories());
        }

        mealRepository.save(meal);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Meal updated successfully"));
    }

    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
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
                meal.getImagePath(),
                meal.getCalories(),
                null,
                meal.getDescription(),
                null
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    public ResponseEntity<?> getAllMeals(Pageable pageable) {
        Page<Meal> meals = mealRepository.findAllPageable(pageable);
        List<MealResponse> mealResponses = meals.getContent().stream()
                .map(w -> new MealResponse(
                        w.getId(),
                        w.getTitle(),
                        w.getImagePath(),
                        w.getCalories(),
                        null,
                        w.getDescription(),
                        null
                ))
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("count", meals.getTotalElements());
        response.put("totalPages", meals.getTotalPages());
        response.put("currentPage", meals.getNumber());
        response.put("meals", mealResponses);

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", response));
    }


    public ResponseEntity<?> uploadMealImage(ImageRequest request) {
        Optional<Meal> meal = mealRepository.findById(request.getId());
        if (meal.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Meal not found"));
        }

        String imagePath = FilesManagement.upload(request.getImage(), request.getId(), "meals");
        if (imagePath == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Upload failed"));
        }

        meal.get().setImagePath(imagePath);
        mealRepository.save(meal.get());

        return ResponseEntity.ok(Map.of("message", "Meal image uploaded", "imageUrl", imagePath));
    }

}
