package com.graduation.GMS.Controllers;

import com.graduation.GMS.DTO.Request.CreateWorkoutRequest;
import com.graduation.GMS.DTO.Request.ImageRequest;
import com.graduation.GMS.DTO.Request.WorkoutRequest;
import com.graduation.GMS.Services.WorkoutService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/workout")
@AllArgsConstructor
public class WorkoutController {

    private WorkoutService workoutService;

    @PostMapping("create")
    public ResponseEntity<?> createWorkout(@Valid @ModelAttribute CreateWorkoutRequest request) {
        return workoutService.createWorkout(request);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateWorkout(@PathVariable Integer id,
                                           @Valid @RequestBody WorkoutRequest request) {
        return workoutService.updateWorkout(id, request);
    }

    @PutMapping("upload-image")
    public ResponseEntity<?> uploadWorkoutImage(@ModelAttribute ImageRequest request){
        return workoutService.uploadWorkoutImage(request);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteWorkout(@PathVariable Integer id) {
        return workoutService.deleteWorkout(id);
    }

    @GetMapping("/show/{id}")
    public ResponseEntity<?> getWorkoutById(@PathVariable Integer id) {
        return workoutService.getWorkoutById(id);
    }

    @GetMapping("/show/all")
    public ResponseEntity<?> getAllWorkouts(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) String muscle,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return workoutService.searchWorkouts(keyword, muscle, pageable);
    }


    // Add to favorites
    @PostMapping("/{id}/add-to-favorite")
    public ResponseEntity<?> addWorkoutToFavorites(@PathVariable("id") Integer workoutId) {
        return workoutService.addWorkoutToFavorites(workoutId);
    }

    // Remove from favorites
    @PostMapping("/{id}/remove-from-favorite")
    public ResponseEntity<?> removeWorkoutFromFavorites(@PathVariable("id") Integer workoutId) {
        return workoutService.removeWorkoutFromFavorites(workoutId);
    }

    // Get all favorites for current user
    @GetMapping("/my-favorites")
    public ResponseEntity<?> getMyFavoriteWorkouts() {
        return workoutService.getMyFavoriteWorkouts();
    }

}