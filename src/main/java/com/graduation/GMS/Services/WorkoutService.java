package com.graduation.GMS.Services;

import com.graduation.GMS.DTO.Request.CreateWorkoutRequest;
import com.graduation.GMS.DTO.Request.ImageRequest;
import com.graduation.GMS.DTO.Request.WorkoutRequest;
import com.graduation.GMS.DTO.Response.WorkoutResponse;
import com.graduation.GMS.Models.Enums.Muscle;
import com.graduation.GMS.Models.User;
import com.graduation.GMS.Models.User_Workout_favorite;
import com.graduation.GMS.Models.Workout;
import com.graduation.GMS.Repositories.User_Workout_FavoriteRepository;
import com.graduation.GMS.Repositories.WorkoutRepository;
import com.graduation.GMS.Handlers.HandleCurrentUserSession;
import com.graduation.GMS.Tools.FilesManagement;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
public class WorkoutService {

    private WorkoutRepository workoutRepository;

    private User_Workout_FavoriteRepository userWorkoutFavorite;

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
    public ResponseEntity<?> createWorkout(CreateWorkoutRequest request) {
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
        workout.setSecondary_muscles(request.getSecondaryMuscle());
        workout.setAvg_calories(request.getAvgCalories());
        workout.setDescription(request.getDescription());

        workoutRepository.save(workout);

//        String imagePath = FilesManagement.upload(request.getImage(), workout.getId(), "workouts");
//        if (imagePath == null) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Map.of("message", "Upload failed"));
//        }
//
//        workout.setImagePath(imagePath);
//        workoutRepository.save(workout);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Workout created successfully"));
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
    public ResponseEntity<?> updateWorkout(Integer id, WorkoutRequest request) {
        Workout workout = workoutRepository.findById(id).orElse(null);
        if (workout == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Workout not found"));
        }

        if (!request.getTitle().isEmpty() && !workout.getTitle().equals(request.getTitle())) {
            workout.setTitle(request.getTitle());
        }

        if (request.getPrimaryMuscle() != null && !workout.getPrimary_muscle().equals(request.getPrimaryMuscle())) {
            workout.setPrimary_muscle(request.getPrimaryMuscle());
        }

        if (request.getSecondaryMuscle() != null && !workout.getSecondary_muscles().equals(request.getSecondaryMuscle())) {
            workout.setSecondary_muscles(request.getSecondaryMuscle());
        }

        if (request.getAvgCalories() != null && !workout.getAvg_calories().equals(request.getAvgCalories())) {
            workout.setAvg_calories(request.getAvgCalories());
        }

        if (!request.getDescription().isEmpty() && !workout.getDescription().equals(request.getDescription())) {
            workout.setDescription(request.getDescription());
        }

        workoutRepository.save(workout);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Workout updated successfully"));
    }

    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
    public ResponseEntity<?> deleteWorkout(Integer id) {
        if (!workoutRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Workout not found"));
        }

        workoutRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK)
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
                0.0f,
                workout.getPrimary_muscle().name(),
                workout.getSecondary_muscles() != null ? workout.getSecondary_muscles().name() : null,
                workout.getAvg_calories(),
                workout.getDescription(),
                workout.getImagePath(),
                0,
                0,
                0
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    public ResponseEntity<?> getAllWorkouts(Muscle muscle, Pageable pageable) {
        Page<Workout> workoutsPage;
        if (muscle != null) {
            workoutsPage = workoutRepository.findByPrimaryMuscle(muscle, pageable);
        } else {
            workoutsPage = workoutRepository.findAllPageable(pageable);
        }
        List<WorkoutResponse> workoutResponses = workoutsPage.stream()
                .map(w -> new WorkoutResponse(
                        w.getId(),
                        w.getTitle(),
                        w.getAvg_calories(),
                        w.getPrimary_muscle().name(),
                        w.getSecondary_muscles() != null ? w.getSecondary_muscles().name() : null,
                        0.0f,
                        w.getDescription(),
                        w.getImagePath(),
                        0,
                        0,
                        0
                ))
                .toList();
        Map<String, Object> result = new HashMap<>();
        result.put("count", workoutsPage.getTotalElements());
        result.put("totalPages", workoutsPage.getTotalPages());
        result.put("currentPage", workoutsPage.getNumber());
        result.put("workouts", workoutResponses);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", result));
    }


    //add to favorite
    @PreAuthorize("hasAnyAuthority('User')")
    public ResponseEntity<?> addWorkoutToFavorites(Integer workoutId) {
        User user = HandleCurrentUserSession.getCurrentUser();

        Workout workout = workoutRepository.findById(workoutId)
                .orElseThrow(() -> new RuntimeException("Workout not found"));

        Optional<User_Workout_favorite> existingFavorite = userWorkoutFavorite.findByUserAndWorkout(user, workout);
        if (existingFavorite.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Workout already in favorites"));
        }

        User_Workout_favorite favorite = new User_Workout_favorite();
        favorite.setUser(user);
        favorite.setWorkout(workout);
        userWorkoutFavorite.save(favorite);

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Workout added to favorites"));
    }

    @PreAuthorize("hasAnyAuthority('User')")
    public ResponseEntity<?> removeWorkoutFromFavorites(Integer workoutId) {
        User user = HandleCurrentUserSession.getCurrentUser();

        Workout workout = workoutRepository.findById(workoutId)
                .orElseThrow(() -> new RuntimeException("Workout not found"));

        Optional<User_Workout_favorite> favoriteOptional = userWorkoutFavorite.findByUserAndWorkout(user, workout);
        if (favoriteOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Workout is not in favorites"));
        }

        userWorkoutFavorite.delete(favoriteOptional.get());

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Workout removed from favorites"));
    }

    @PreAuthorize("hasAnyAuthority('User')")
    public ResponseEntity<?> getMyFavoriteWorkouts() {
        List<User_Workout_favorite> favorites = userWorkoutFavorite.findAllByUser(HandleCurrentUserSession.getCurrentUser());
        List<WorkoutResponse> responseList = favorites.stream()
                .map(fav -> {
                    Workout w = fav.getWorkout();
                    return new WorkoutResponse(
                            w.getId(),
                            w.getTitle(),
                            0.0f,
                            w.getPrimary_muscle().name(),
                            w.getSecondary_muscles() != null ? w.getSecondary_muscles().name() : null,
                            w.getAvg_calories(),
                            w.getDescription(),
                            w.getImagePath(),
                            0,
                            0,
                            0
                    );
                })
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK).body(responseList);
    }


    public ResponseEntity<?> uploadWorkoutImage(ImageRequest request) {
        Optional<Workout> workout = workoutRepository.findById(request.getId());
        if (workout.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Workout not found"));
        }

        String imagePath = FilesManagement.upload(request.getImage(), request.getId(), "workouts");
        if (imagePath == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Upload failed"));
        }

        workout.get().setImagePath(imagePath);
        workoutRepository.save(workout.get());

        return ResponseEntity.ok(Map.of("message", "workout image uploaded", "imageUrl", imagePath));
    }

}
