package com.graduation.GMS.Controllers;

import com.graduation.GMS.DTO.Request.*;
import com.graduation.GMS.Services.DietService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api/diet")
@AllArgsConstructor
public class DietController {

    private DietService dietService;

    @PostMapping("/create")
    public ResponseEntity<?> createDiet(@Valid @RequestBody DietRequest dietRequest) {
        return dietService.createDiet(dietRequest);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateDiet(@PathVariable Integer id, @Valid @RequestBody DietRequest dietRequest) {
        return dietService.updateDiet(id, dietRequest);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteDiet(@PathVariable Integer id) {
        return dietService.deleteDiet(id);
    }

    @GetMapping("/show/{id}")
    public ResponseEntity<?> getDietById(@PathVariable Integer id) {
        return dietService.getDietById(id);
    }

    @GetMapping("/show/all")
    public ResponseEntity<?> getAllDiets(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (page == null || size == null) {
            // No pagination
            return dietService.getAllDiets(null);
        } else {
            Pageable pageable = PageRequest.of(page, size);
            return dietService.getAllDiets(pageable);
        }
    }

    @PostMapping("/assign-meal")
    public ResponseEntity<?> assignMealToDiet(@RequestBody @Valid AssignMealToDietRequest request) {
        return dietService.assignMealToDiet(request);
    }

    @PutMapping("/update-assign-meal")
    public ResponseEntity<?> updateAssignMealToDiet(@RequestBody @Valid AssignMealToDietRequest request) {
        return dietService.updateAssignedMealToDiet(request);
    }

    @DeleteMapping("/unassign-meal")
    public ResponseEntity<?> unAssignMealToDiet(@RequestBody @Valid AssignMealToDietRequest request) {
        return dietService.unAssignMealFromDiet(request);
    }


    @PostMapping("/assign")
    public ResponseEntity<?> assignDietToUser(@RequestBody @Valid AssignDietToUser request) {
        return dietService.assignDietToUser(request);
    }

    @DeleteMapping("/unassign")
    public ResponseEntity<?> unAssignDietToUser(@RequestBody @Valid AssignDietToUser request) {
        return dietService.unAssignDietToUser(request);
    }

    @PostMapping("/rate")
    public ResponseEntity<?> rateDietPlan(@RequestBody @Valid RateDietRequest request) {
        return dietService.rateDietPlan(request);
    }

    @PostMapping("/feedback")
    public ResponseEntity<?> submitFeedback(@RequestBody @Valid FeedBackDietRequest request) {
        return dietService.submitFeedback(request);
    }

    @GetMapping("/{dietId}/feedbacks")
    public ResponseEntity<?> getAllDietPlanFeedBacks(@PathVariable Integer dietId) {
        return dietService.getAllDietPlanFeedBacks(dietId);
    }

    @GetMapping("/my-diets")
    public ResponseEntity<?> getMyAssignedDiets() {
        return dietService.getMyAssignedDiets();
    }

    @GetMapping("/assigned/{userId}")
    public ResponseEntity<?> getAssignedDietsByUserId(@PathVariable Integer userId) {
        return dietService.getAssignedDietsByUserId(userId);
    }

    @GetMapping("/get-members-in-DietPlan/{dietId}")
    public ResponseEntity<?> getMembersInDietPlan(@PathVariable Integer dietId) {
        return dietService.getDietSubscribers(dietId);
    }

    @DeleteMapping("/delete-user-feedback")
    public ResponseEntity<?> deleteUserFeedback(@RequestBody Map<String, String> data) {
        return dietService.deleteDietFeedback(Integer.valueOf(data.get("userId")), Integer.valueOf(data.get("dietId")));
    }

    @GetMapping("/get-user-subscription-diets/{userId}")
    public ResponseEntity<?> getUserSubscriptionDiets(@PathVariable Integer userId) {
        return dietService.getUserSubscriptionDiets(userId);
    }

}
