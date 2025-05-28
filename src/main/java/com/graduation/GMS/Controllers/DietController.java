package com.graduation.GMS.Controllers;

import com.graduation.GMS.DTO.Request.*;
import com.graduation.GMS.Services.DietService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/diet")
@AllArgsConstructor
public class DietController {

    private DietService dietService;

    // Endpoint to create a new diet
    @PostMapping("/create")
    public ResponseEntity<?> createDiet(@Valid @RequestBody DietRequest dietRequest) {
        return dietService.createDiet(dietRequest);
    }

    // Endpoint to update an existing diet
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateDiet(@PathVariable Integer id, @Valid @RequestBody DietRequest dietRequest) {
        return dietService.updateDiet(id, dietRequest);
    }

    // Endpoint to delete a diet
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteDiet(@PathVariable Integer id) {
        return dietService.deleteDiet(id);
    }

    // Endpoint to get details of a specific diet by ID
    @GetMapping("/show/{id}")
    public ResponseEntity<?> getDietById(@PathVariable Integer id) {
        return dietService.getDietById(id);
    }

    // Endpoint to get all diets
    @GetMapping("/show/all")
    public ResponseEntity<?> getAllDiets() {
        return dietService.getAllDiets();
    }

    // Endpoint to assign a meal for a diet (Request body)
    @PostMapping("/assign-meal")
    public ResponseEntity<?> assignMealToDiet(@RequestBody @Valid AssignMealToDietRequest request) {
        return dietService.assignMealToDiet(request);
    }
    // Endpoint to update assign a meal for a diet (Request body)
    @PostMapping("/update-assign-meal")
    public ResponseEntity<?> updateAssignMealToDiet(@RequestBody @Valid AssignMealToDietRequest request) {
        return dietService.updateAssignedMealToDiet(request);
    }
    // Endpoint to Unassign a meal for a diet (Request body)
    @PostMapping("/unassign-meal")
    public ResponseEntity<?> unAssignMealToDiet(@RequestBody @Valid AssignMealToDietRequest request) {
        return dietService.unAssignMealFromDiet(request);
    }


    // User Diet Assignment Endpoints
    @PostMapping("/assign")
    public ResponseEntity<?> assignDietToUser(@RequestBody @Valid AssignDietToUser request) {
        return dietService.assignDietToUser(request);
    }

    @PostMapping("/unassign")
    public ResponseEntity<?> unAssignDietToUser(@RequestBody @Valid AssignDietToUser request) {
        return dietService.unAssignDietToUser(request);
    }

    // Rating and Feedback Endpoints
    @PostMapping("/rate")
    public ResponseEntity<?> rateDietPlan(@RequestBody @Valid RateDietRequest request) {
        return dietService.rateDietPlan(request);
    }

    @PostMapping("/feedback")
    public ResponseEntity<?> submitFeedback(@RequestBody @Valid FeedBackDietRequest request) {
        return dietService.submitFeedback(request);
    }

    // Get all feedbacks for a specific diet Plan
    @GetMapping("/{dietId}/feedbacks")
    public ResponseEntity<?> getAllDietPlanFeedBacks(@PathVariable Integer dietId) {
        return dietService.getAllDietPlanFeedBacks(dietId);
    }

    // User Diet Endpoints
    @GetMapping("/my-diets")
    public ResponseEntity<?> getMyAssignedDiets() {
        return dietService.getMyAssignedDiets();
    }

    @GetMapping("/assigned/{userId}")
    public ResponseEntity<?> getAssignedDietsByUserId(@PathVariable Integer userId) {
        return dietService.getAssignedDietsByUserId(userId);
    }
}
