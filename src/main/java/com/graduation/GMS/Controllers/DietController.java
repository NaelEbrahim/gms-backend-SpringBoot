package com.graduation.GMS.Controllers;

import com.graduation.GMS.DTO.Request.AssignMealToDietRequest;
import com.graduation.GMS.DTO.Request.DietRequest;
import com.graduation.GMS.Services.DietService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/diet")
public class DietController {

    @Autowired
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
}
