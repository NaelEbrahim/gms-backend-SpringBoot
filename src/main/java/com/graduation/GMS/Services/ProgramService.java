package com.graduation.GMS.Services;

import com.graduation.GMS.DTO.Request.ProgramRequest;
import com.graduation.GMS.DTO.Request.AssignWorkoutToProgramRequest;
import com.graduation.GMS.DTO.Response.ProgramResponse;
import com.graduation.GMS.DTO.Response.WorkoutResponse;
import com.graduation.GMS.Models.*;
import com.graduation.GMS.Repositories.*;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProgramService {

    private ProgramRepository programRepository;

    private WorkoutRepository workoutRepository;

    private Program_WorkoutRepository programWorkoutRepository;

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
    public ResponseEntity<?> createProgram(ProgramRequest request) {
        // Check if program title already exists
        Optional<Program> existingProgram = programRepository.findByTitle(request.getTitle());
        if (existingProgram.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Program title already exists"));
        }

        // Create and save new program
        Program program = new Program();
        program.setTitle(request.getTitle());
        program.setLevel(request.getLevel());
        if (request.getIsPublic().equalsIgnoreCase("true")) {
            program.setIsPublic(true);
        }
        else {
            program.setIsPublic(false);
        }
        programRepository.save(program);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Program created successfully"));
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
    public ResponseEntity<?> updateProgram(Integer id, ProgramRequest request) {
        Optional<Program> optionalProgram = programRepository.findById(id);
        if (optionalProgram.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Program not found"));
        }

        Program program = optionalProgram.get();
        // Check if program title already exists
        Optional<Program> existingProgram = programRepository.findByTitle(request.getTitle());
        if (existingProgram.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Program title already exists"));
        }
        if(!program.getTitle().equals(request.getTitle()) && !request.getTitle().isEmpty()) {
            program.setTitle(request.getTitle());
        }
        if(!program.getLevel().equals(request.getLevel()) && request.getLevel() != null) {
            program.setLevel(request.getLevel());        }
        if(!program.getIsPublic().equals(request.getIsPublic()) && !request.getIsPublic().isEmpty()) {
            if (request.getIsPublic().equalsIgnoreCase("true")) {
                program.setIsPublic(true);
            }
            else {
                program.setIsPublic(false);
            }
        }

        programRepository.save(program);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Program updated successfully"));
    }

    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
    public ResponseEntity<?> deleteProgram(Integer id) {
        if (!programRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Program not found"));
        }

        programRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Program deleted successfully"));
    }
    public ResponseEntity<?> getProgramById(Integer id) {
        Optional<Program> programOptional = programRepository.findById(id);
        if (programOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Program not found"));
        }

        Program program = programOptional.get();

        // Get all workouts associated with this program
        List<WorkoutResponse> workoutResponses = programWorkoutRepository.findByProgram(program)
                .stream()
                .map(programWorkout -> {
                    Workout workout = programWorkout.getWorkout();
                    return new WorkoutResponse(
                            workout.getId(),
                            workout.getTitle(),
                            workout.getPrimary_muscle(),
                            workout.getSecondary_muscles(),
                            workout.getAvg_calories(),
                            workout.getDescription(),
                            programWorkout.getReps(),
                            programWorkout.getSets()
                    );
                })
                .toList();

        ProgramResponse response = new ProgramResponse(
                program.getId(),
                program.getTitle(),
                program.getLevel(),
                program.getIsPublic(),
                workoutResponses
        );

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    public ResponseEntity<?> getAllPrograms() {
        List<Program> programs = programRepository.findAll();

        if (programs.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No programs found"));
        }

        List<ProgramResponse> programResponses = programs.stream()
                .map(program -> {
                    // Get workouts for each program
                    List<WorkoutResponse> workoutResponses = programWorkoutRepository.findByProgram(program)
                            .stream()
                            .map(programWorkout -> {
                                Workout workout = programWorkout.getWorkout();
                                return new WorkoutResponse(
                                        workout.getId(),
                                        workout.getTitle(),
                                        workout.getPrimary_muscle(),
                                        workout.getSecondary_muscles(),
                                        workout.getAvg_calories(),
                                        workout.getDescription(),
                                        programWorkout.getReps(),
                                        programWorkout.getSets()
                                );
                            })
                            .collect(Collectors.toList());

                    return new ProgramResponse(
                            program.getId(),
                            program.getTitle(),
                            program.getLevel(),
                            program.getIsPublic(),
                            workoutResponses
                    );
                })
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK).body(programResponses);
    }


    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
    public ResponseEntity<?> assignWorkoutToProgram(AssignWorkoutToProgramRequest request) {
        Optional<Program> programOptional = programRepository.findById(request.getProgramId());
        if (programOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Program not found"));
        }

        Optional<Workout> workoutOptional = workoutRepository.findById(request.getWorkoutId());
        if (workoutOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Workout not found"));
        }

        Program program = programOptional.get();
        Workout workout = workoutOptional.get();

        // Check if already assigned
        boolean exists = programWorkoutRepository.existsByProgramAndWorkout(program, workout);
        if (exists) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Workout is already assigned to this program"));
        }

        // Create and save the relationship
        Program_Workout programWorkout = new Program_Workout();
        programWorkout.setProgram(program);
        programWorkout.setWorkout(workout);
        programWorkout.setReps(request.getReps());
        programWorkout.setSets(request.getSets());

        programWorkoutRepository.save(programWorkout);

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Workout successfully assigned to program"));
    }


}