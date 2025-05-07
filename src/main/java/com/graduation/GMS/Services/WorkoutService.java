package com.graduation.GMS.Services;

import com.graduation.GMS.DTO.Request.WorkoutRequest;
import com.graduation.GMS.DTO.Response.WorkoutResponse;
import com.graduation.GMS.Models.Workout;
import com.graduation.GMS.Repositories.WorkoutRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WorkoutService {
    @Autowired
    private WorkoutRepository workoutRepository;

    @Transactional
    public ResponseEntity<?> createWorkout(WorkoutRequest request) {
        // Check if workout title already exists
        Optional<Workout> existingWorkout = workoutRepository.findByTitle(request.getTitle());
        if (existingWorkout.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Workout title already exists"));
        }

        // Create and save new workout
        Workout workout = new Workout();
        workout.setTitle(request.getTitle());
        workout.setPrimary_muscle(request.getPrimaryMuscle());
        workout.setSecondary_muscles(request.getSecondaryMuscles());
        workout.setAvg_calories(request.getAvgCalories());
        workout.setDescription(request.getDescription());

        workoutRepository.save(workout);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Workout created successfully"));
    }

    public ResponseEntity<?> updateWorkout(Integer id, WorkoutRequest request) {
        Optional<Workout> optionalWorkout = workoutRepository.findById(id);
        if (optionalWorkout.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Workout not found"));
        }

        Workout workout = optionalWorkout.get();


        if (!request.getTitle().isEmpty()&&!workout.getTitle().equals(request.getTitle())) {
            workout.setTitle(request.getTitle());
        }

        if (!request.getPrimaryMuscle().isEmpty() && !workout.getPrimary_muscle().equals(request.getPrimaryMuscle())) {
            workout.setPrimary_muscle(request.getPrimaryMuscle());
        }

        if (!request.getSecondaryMuscles().isEmpty() && !workout.getSecondary_muscles().equals(request.getSecondaryMuscles())) {
            workout.setSecondary_muscles(request.getSecondaryMuscles());
        }

        if (!request.getAvgCalories().isEmpty() && !workout.getAvg_calories().equals(request.getAvgCalories())) {
            workout.setAvg_calories(request.getAvgCalories());
        }

        if (!request.getDescription().isEmpty() && !workout.getDescription().equals(request.getDescription())) {
            workout.setDescription(request.getDescription());
        }

        workoutRepository.save(workout);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Workout updated successfully"));
    }

    public ResponseEntity<?> deleteWorkout(Integer id) {
        if (!workoutRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Workout not found"));
        }

        workoutRepository.deleteById(id);
        return  ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Workout deleted successfully"));

    }

    public ResponseEntity<?> getWorkoutById(Integer id) {
        Optional<Workout> workoutOptional = workoutRepository.findById(id);
        if (workoutOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Workout not found"));
        }

        Workout workout = workoutOptional.get();
        WorkoutResponse response = new WorkoutResponse(
                workout.getId(),
                workout.getTitle(),
                workout.getPrimary_muscle(),
                workout.getSecondary_muscles(),
                workout.getAvg_calories(),
                workout.getDescription()
        );

        return  ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    public ResponseEntity<?> getAllWorkouts() {
        List<Workout> workouts = workoutRepository.findAll();

        if (workouts.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No workouts found"));
        }

        List<WorkoutResponse> workoutResponses = workouts.stream()
                .map(w -> new WorkoutResponse(
                        w.getId(),
                        w.getTitle(),
                        w.getPrimary_muscle(),
                        w.getSecondary_muscles(),
                        w.getAvg_calories(),
                        w.getDescription()
                ))
                .collect(Collectors.toList());

        return  ResponseEntity.status(HttpStatus.OK)
                .body(workoutResponses);
    }

}