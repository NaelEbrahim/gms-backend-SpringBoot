package com.graduation.GMS.Controllers;
import com.graduation.GMS.DTO.Request.ImageRequest;
import com.graduation.GMS.DTO.Request.MealRequest;
import com.graduation.GMS.Services.MealService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/meal")
@AllArgsConstructor
public class MealController {

    private MealService mealService;

    // Endpoint to create a new Meal
    @PostMapping("/create")
    public ResponseEntity<?> createMeal(@Valid @RequestBody MealRequest request) {
        return mealService.createMeal(request);
    }

    // Endpoint to update an existing Meal
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateMeal(@PathVariable Integer id, @Valid @RequestBody MealRequest request) {
        return mealService.updateMeal(id, request);
    }

    @PutMapping("upload-image")
    public ResponseEntity<?> uploadMealImage(@ModelAttribute ImageRequest request){
        return mealService.uploadMealImage(request);
    }

    // Endpoint to delete an Meal
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteMeal(@PathVariable Integer id) {
        return mealService.deleteMeal(id);
    }

    // Endpoint to get details of a specific Meal by ID
    @GetMapping("/show/{id}")
    public ResponseEntity<?> getMealById(@PathVariable Integer id) {
        return mealService.getMealById(id);
    }

    // Endpoint to get all Meals
    @GetMapping("/show/all")
    public ResponseEntity<?> getAllMeals() {
        return mealService.getAllMeals();
    }

}

