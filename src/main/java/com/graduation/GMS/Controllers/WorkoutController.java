package com.graduation.GMS.Controllers;

import com.graduation.GMS.DTO.Request.WorkoutRequest;
import com.graduation.GMS.Services.WorkoutService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/workout")
public class WorkoutController {

    @Autowired
    private WorkoutService workoutService;

    @PostMapping("create")
    public ResponseEntity<?> createWorkout(@Valid @RequestBody WorkoutRequest request) {
        return workoutService.createWorkout(request);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateWorkout(@PathVariable Integer id,
                                           @Valid @RequestBody WorkoutRequest request) {
        return workoutService.updateWorkout(id, request);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteWorkout(@PathVariable Integer id) {
        return workoutService.deleteWorkout(id);
    }

    @GetMapping("/show/{id}")
    public ResponseEntity<?> getWorkoutById(@PathVariable Integer id) {
        return workoutService.getWorkoutById(id);
    }

    @GetMapping("show/all")
    public ResponseEntity<?> getAllWorkouts() {
        return workoutService.getAllWorkouts();
    }

}