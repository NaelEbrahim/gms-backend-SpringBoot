package com.graduation.GMS.Controllers;

import com.graduation.GMS.DTO.Request.CreateMealRequest;
import com.graduation.GMS.DTO.Request.ImageRequest;
import com.graduation.GMS.DTO.Request.MealRequest;
import com.graduation.GMS.Services.MealService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/meal")
@AllArgsConstructor
public class MealController {

    private MealService mealService;

    @PostMapping("/create")
    public ResponseEntity<?> createMeal(@Valid @ModelAttribute CreateMealRequest request) {
        return mealService.createMeal(request);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateMeal(@PathVariable Integer id, @Valid @ModelAttribute MealRequest request) {
        return mealService.updateMeal(id, request);
    }

    @PutMapping("upload-image")
    public ResponseEntity<?> uploadMealImage(@ModelAttribute ImageRequest request) {
        return mealService.uploadMealImage(request);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteMeal(@PathVariable Integer id) {
        return mealService.deleteMeal(id);
    }

    @GetMapping("/show/{id}")
    public ResponseEntity<?> getMealById(@PathVariable Integer id) {
        return mealService.getMealById(id);
    }

    @GetMapping("/show/all")
    public ResponseEntity<?> getAllMeals(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (page == null || size == null) {
            // No pagination
            return mealService.getAllMeals(null);
        } else {
            Pageable pageable = PageRequest.of(page, size);
            return mealService.getAllMeals(pageable);
        }
    }

}

