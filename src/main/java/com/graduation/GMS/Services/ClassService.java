package com.graduation.GMS.Services;

import com.graduation.GMS.DTO.Request.AssignProgramToClassRequest;
import com.graduation.GMS.DTO.Request.ClassRequest;
import com.graduation.GMS.DTO.Response.ClassResponse;
import com.graduation.GMS.DTO.Response.ProgramResponse;
import com.graduation.GMS.DTO.Response.UserResponse;
import com.graduation.GMS.DTO.Response.WorkoutResponse;
import com.graduation.GMS.Models.*;
import com.graduation.GMS.Models.Class;
import com.graduation.GMS.Repositories.*;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class ClassService {

    private UserRepository userRepository;

    private  ClassRepository classRepository;

    private  ProgramRepository programRepository;

    private Class_ProgramRepository class_ProgramRepository;

    private Program_WorkoutRepository programWorkoutRepository;



    @Transactional
    public ResponseEntity<?> createClass(@Valid ClassRequest request) {
        // Check if the class title already exists (optional validation)
        Optional<Class> existingClass = classRepository.findByName(request.getName());
        if (existingClass.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Class title already exists"));
        }
        // Convert the DTO to entity and save
        Class classEntity = new Class();
        Optional<User> coach =userRepository.findById(request.getCoachId());
        if (coach.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Coach Not Found"));
        }
        classEntity.setAuditCoach(coach.get());
        classEntity.setName(request.getName());
        classEntity.setDescription(request.getDescription());
        classEntity.setPrice(request.getPrice());

        // Save the class to the database
        classRepository.save(classEntity);
        // Return the response with the saved class details
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Class created successfully"));
    }

    // Update an existing class
    @Transactional
    public ResponseEntity<?> updateClass(Integer id, ClassRequest request) {
        Optional<Class> optionalClass = classRepository.findById(id);
        if (optionalClass.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Class not found"));
        }

        Class existingClass = optionalClass.get();

        Optional<User> coach =userRepository.findById(request.getCoachId());
        if (coach.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Coach Not Found"));
        }

        if (!existingClass.getAuditCoach().getId().equals(request.getCoachId())) {
            existingClass.setAuditCoach(coach.get());
        }

        if (!existingClass.getName().equals(request.getName())&&!request.getName().isEmpty()) {
            existingClass.setName(request.getName());
        }
        if (!existingClass.getDescription().equals(request.getDescription())&&!request.getDescription().isEmpty()) {
            existingClass.setDescription(request.getName());
        }
        if (!existingClass.getPrice().equals(request.getPrice())&&request.getPrice()!=null) {
            existingClass.setPrice(request.getPrice());
        }
        classRepository.save(existingClass);

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Class updated successfully"));
    }

    // Delete a class
    public ResponseEntity<?> deleteClass(Integer id) {
        if (!classRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Class not found"));
        }

        classRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Class deleted successfully"));
    }

    public ResponseEntity<?> getClassById(Integer classId) {
        Optional<Class> classOptional = classRepository.findById(classId);
        if (classOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Class not found"));
        }

        Class classEntity = classOptional.get();
        UserResponse coachResponse = mapToUserResponse(classEntity.getAuditCoach());
        // Get all programs associated with this class
        List<ProgramResponse> programResponses = class_ProgramRepository.findByAClass(classEntity)
                .stream()
                .map(classProgram -> {
                    Program program = classProgram.getProgram();
                    ;
                    // Get all workouts for each program
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
                                        workout.getDescription()
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

        ClassResponse responseDto = new ClassResponse(
                coachResponse,
                classEntity.getId(),
                classEntity.getName(),
                classEntity.getDescription(),
                classEntity.getPrice(),
                programResponses
        );

        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    public ResponseEntity<?> getAllClasses() {
        List<Class> classes = classRepository.findAll();

        if (classes.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No classes found"));
        }

        List<ClassResponse> classResponses = classes.stream()
                .map(classEntity -> {
                    // Get all programs for each class
                    List<ProgramResponse> programResponses = class_ProgramRepository.findByAClass(classEntity)
                            .stream()
                            .map(classProgram -> {
                                Program program = classProgram.getProgram();

                                // Get all workouts for each program
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
                                                    workout.getDescription()
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

                    return new ClassResponse(
                            mapToUserResponse(classEntity.getAuditCoach()),
                            classEntity.getId(),
                            classEntity.getName(),
                            classEntity.getDescription(),
                            classEntity.getPrice(),
                            programResponses
                    );
                })
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK).body(classResponses);
    }

    // Method to assign a program to a class

    @Transactional
    public ResponseEntity<?> assignProgramToClass(@Valid AssignProgramToClassRequest request) {
        Integer classId = request.getClassId();
        Integer programId = request.getProgramId();

        Optional<Class> classOptional = classRepository.findById(classId);
        if (classOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Class not found"));
        }

        Optional<Program> programOptional = programRepository.findById(programId);
        if (programOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Program not found"));
        }

        Class classEntity = classOptional.get();
        Program programEntity = programOptional.get();

        // Optional: Check if already assigned
        boolean exists = class_ProgramRepository.existsByAClassAndProgram(classEntity, programEntity);
        if (exists) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Program is already assigned to this class"));
        }

        // Create and save the relationship
        Class_Program classProgram = new Class_Program();
        classProgram.setAClass(classEntity);
        classProgram.setProgram(programEntity);

        class_ProgramRepository.save(classProgram);

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Program successfully assigned to class"));
    }
    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getGender(),
                user.getDob(),
                user.getCreatedAt(),
                user.getQr()
        );
    }

}
