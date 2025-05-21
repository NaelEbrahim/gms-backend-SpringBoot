package com.graduation.GMS.Services;

import com.graduation.GMS.DTO.Request.*;
import com.graduation.GMS.DTO.Response.*;
import com.graduation.GMS.Models.*;
import com.graduation.GMS.Models.Class;
import com.graduation.GMS.Repositories.*;
import com.graduation.GMS.Tools.HandleCurrentUserSession;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.graduation.GMS.DTO.Response.UserResponse.mapToUserResponse;

@Service
@AllArgsConstructor
public class ProgramService {

    private ProgramRepository programRepository;

    private WorkoutRepository workoutRepository;

    private Program_WorkoutRepository programWorkoutRepository;

    private User_ProgramRepository userProgramRepository;

    private UserRepository userRepository;

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
                calculateRate(program.getId()),
                workoutResponses,
                null
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
                            calculateRate(program.getId()),
                            workoutResponses,
                            null
                    );
                })
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK).body(programResponses);
    }
    private Float calculateRate(int programId) {
        // First check if the program exists
        Optional<Program> programOptional = programRepository.findById(programId);
        if (programOptional.isEmpty()) {
            return 0.0F;
        }

        // Get all User_Program entries for this program
        List<User_Program> userPrograms = userProgramRepository.findByProgram(programOptional.get());

        if (userPrograms.isEmpty()) {
            return 0.0F;
        }

        // Calculate average rating using stream API
        Double average = userPrograms.stream()
                .filter(up -> up.getRate() != null)  // Filter out null ratings
                .mapToDouble(User_Program::getRate)  // Convert to double for calculation
                .average()                           // Calculate average
                .orElse(0.0);                        // Default to 0.0 if no ratings

        return average.floatValue();  // Convert back to Float
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

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
    public ResponseEntity<?> assignProgramToUser(AssignProgramToUser request) {
        Optional<User> userOptional = userRepository.findById(request.getUserId());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        }

        Optional<Program> programOptional = programRepository.findById(request.getProgramId());
        if (programOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Program not found"));
        }

        Program program = programOptional.get();
        User user = userOptional.get();

        // Check if already assigned
        boolean exists = userProgramRepository.existsByUserAndProgram(user, program);
        if (exists) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Program is already assigned to this user"));
        }

        // Create and save the relationship
        User_Program userProgram = new User_Program();
        userProgram.setProgram(program);
        userProgram.setUser(user);
        userProgram.setInitializedAt(LocalDateTime.now());
        userProgram.setExpiryDate(LocalDateTime.now().plusMonths(1));
        userProgram.setIsActive(true);

        userProgramRepository.save(userProgram);

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "program successfully assigned to user"));
    }
    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
    public ResponseEntity<?> unAssignProgramToUser(AssignProgramToUser request) {
        Optional<User> userOptional = userRepository.findById(request.getUserId());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        }

        Optional<Program> programOptional = programRepository.findById(request.getProgramId());
        if (programOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Program not found"));
        }

        Program program = programOptional.get();
        User user = userOptional.get();

        // Find the existing assignment
        Optional<User_Program> userProgramOptional = userProgramRepository.findByUserAndProgram(user, program);
        if (userProgramOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Program is not assigned to this user"));
        }

        // Delete the assignment
        userProgramRepository.delete(userProgramOptional.get());

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Program successfully unassigned from user"));
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('User')")
    public ResponseEntity<?> rateProgram(RateProgramRequest request) {

        User user = HandleCurrentUserSession.getCurrentUser();

        // Validate program exists
        Optional<Program> programOptional = programRepository.findById(request.getProgramId());
        if (programOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Program not found"));
        }

        Program program = programOptional.get();

        // Check if program is assigned to user
        if (!userProgramRepository.existsByUserAndProgram(user, program)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You can only rate programs assigned to you"));
        }

        // Validate rating (1-5)
        if (request.getRate() == null || request.getRate() < 1 || request.getRate() > 5) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Rating must be between 1 and 5"));
        }

        // Find or create user-program relationship
        User_Program userProgram = userProgramRepository.findByUserAndProgram(user, program)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        // Update rating
        userProgram.setRate(request.getRate());
        userProgramRepository.save(userProgram);


        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Program rated successfully"));
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('User')")
    public ResponseEntity<?> submitFeedback(FeedBackProgramRequest request) {

        User user = HandleCurrentUserSession.getCurrentUser();

        // Validate program exists
        Optional<Program> programOptional = programRepository.findById(request.getProgramId());
        if (programOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Program not found"));
        }

        Program program = programOptional.get();

        // Check if program is assigned to user
        if (!userProgramRepository.existsByUserAndProgram(user, program)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You can only submit feedback for programs assigned to you"));
        }

        // Validate feedback not empty
        if (request.getFeedback() == null || request.getFeedback().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Feedback cannot be empty"));
        }

        // Find user-program relationship
        User_Program userProgram = userProgramRepository.findByUserAndProgram(user, program)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        // Update feedback
        userProgram.setFeedback(request.getFeedback());
        userProgramRepository.save(userProgram);

        return ResponseEntity.ok(Map.of("message", "Feedback submitted successfully"));
    }

    public ResponseEntity<?> getAllProgramFeedBacks(Integer programId) {
        Optional<Program> programOptional = programRepository.findById(programId);
        if (programOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Program not found"));
        }

        Program program = programOptional.get();

        // Get only active subscribers
        List<UserFeedBackResponse> feedBacks = userProgramRepository.findFeedbackByProgram(program)
                .stream()
                .map(User_Program -> {
                    User user = User_Program.getUser();
                    return new UserFeedBackResponse(user,User_Program.getFeedback());
                })
                .toList();

        ProgramResponse responseDto = new ProgramResponse(
                program.getId(),
                program.getTitle(),
                program.getLevel(),
                program.getIsPublic(),
                calculateRate(program.getId()),
                null,
                feedBacks
        );
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @PreAuthorize("hasAnyAuthority('User')")
    public ResponseEntity<?> getMyAssignedPrograms() {
        User currentUser = HandleCurrentUserSession.getCurrentUser();

        List<User_Program> userPrograms = userProgramRepository.findByUser(currentUser);

        if (userPrograms.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No programs assigned to the current user"));
        }

        List<ProgramResponse> programResponses = userPrograms.stream()
                .map(up -> {
                    Program program = up.getProgram();

                    List<WorkoutResponse> workouts = programWorkoutRepository.findByProgram(program)
                            .stream()
                            .map(pw -> {
                                Workout workout = pw.getWorkout();
                                return new WorkoutResponse(
                                        workout.getId(),
                                        workout.getTitle(),
                                        workout.getPrimary_muscle(),
                                        workout.getSecondary_muscles(),
                                        workout.getAvg_calories(),
                                        workout.getDescription(),
                                        pw.getReps(),
                                        pw.getSets()
                                );
                            })
                            .toList();

                    return new ProgramResponse(
                            program.getId(),
                            program.getTitle(),
                            program.getLevel(),
                            program.getIsPublic(),
                            calculateRate(program.getId()),
                            workouts,
                            null
                    );
                })
                .toList();

        return ResponseEntity.ok(programResponses);
    }

    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
    public ResponseEntity<?> getAssignedProgramsByUserId(Integer userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        }

        User user = userOptional.get();
        List<User_Program> userPrograms = userProgramRepository.findByUser(user);

        if (userPrograms.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No programs assigned to this user"));
        }

        List<ProgramResponse> programResponses = userPrograms.stream()
                .map(up -> {
                    Program program = up.getProgram();

                    List<WorkoutResponse> workouts = programWorkoutRepository.findByProgram(program)
                            .stream()
                            .map(pw -> {
                                Workout workout = pw.getWorkout();
                                return new WorkoutResponse(
                                        workout.getId(),
                                        workout.getTitle(),
                                        workout.getPrimary_muscle(),
                                        workout.getSecondary_muscles(),
                                        workout.getAvg_calories(),
                                        workout.getDescription(),
                                        pw.getReps(),
                                        pw.getSets()
                                );
                            })
                            .toList();

                    return new ProgramResponse(
                            program.getId(),
                            program.getTitle(),
                            program.getLevel(),
                            program.getIsPublic(),
                            calculateRate(program.getId()),
                            workouts,
                            null
                    );
                })
                .toList();

        return ResponseEntity.ok(programResponses);
    }



}