package com.graduation.GMS.Services;

import com.graduation.GMS.DTO.Request.*;
import com.graduation.GMS.DTO.Response.*;
import com.graduation.GMS.Models.*;
import com.graduation.GMS.Models.Enums.Day;
import com.graduation.GMS.Models.Enums.Muscle;
import com.graduation.GMS.Models.Enums.Roles;
import com.graduation.GMS.Repositories.*;
import com.graduation.GMS.Handlers.HandleCurrentUserSession;
import com.graduation.GMS.Services.GeneralServices.NotificationService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
public class ProgramService {

    private ProgramRepository programRepository;

    private WorkoutRepository workoutRepository;

    private Program_WorkoutRepository programWorkoutRepository;

    private User_ProgramRepository userProgramRepository;

    private UserRepository userRepository;

    private NotificationService notificationService;

    private NotificationRepository notificationRepository;

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
    public ResponseEntity<?> createProgram(ProgramRequest request) {
        // Check if program title already exists
        Optional<Program> existingProgram = programRepository.findByTitle(request.getTitle());
        if (existingProgram.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Program title already exists"));
        }

        // Create and save new program
        Program program = new Program();
        program.setTitle(request.getTitle());
        program.setLevel(request.getLevel());
        if (request.getIsPublic().equalsIgnoreCase("true")) {
            program.setIsPublic(true);
        } else {
            program.setIsPublic(false);
        }

        // Create and send notification
        Notification notification = new Notification();
        notification.setTitle("New Program");
        notification.setContent("New Program has been made :" + program.getTitle());
        notification.setCreatedAt(LocalDateTime.now());
        // Persist notification first
        notification = notificationRepository.save(notification); // Save and get managed instance

        List<User> usersWithUserRole = userRepository.findAllByRoleName(Roles.User);

        notificationService.sendNotificationToUsers(
                usersWithUserRole,
                notification
        );
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
        if (!program.getTitle().equals(request.getTitle()) && !request.getTitle().isEmpty()) {
            program.setTitle(request.getTitle());
        }
        if (request.getLevel() != null && !program.getLevel().equals(request.getLevel())) {
            program.setLevel(request.getLevel());
        }
        if (!request.getIsPublic().isEmpty() && !program.getIsPublic().equals(request.getIsPublic())) {
            if (request.getIsPublic().equalsIgnoreCase("true")) {
                program.setIsPublic(true);
            } else {
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

        ProgramResponse response = new ProgramResponse(
                program.getId(),
                program.getTitle(),
                program.getLevel(),
                program.getIsPublic(),
                calculateRate(program.getId()),
                buildProgramScheduleResponse(program),
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

                    return new ProgramResponse(
                            program.getId(),
                            program.getTitle(),
                            program.getLevel(),
                            program.getIsPublic(),
                            calculateRate(program.getId()),
                            buildProgramScheduleResponse(program),
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
        Optional<Program_Workout> exists = programWorkoutRepository.findByProgramAndWorkoutAndDay(program, workout, request.getDay());
        if (exists.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Workout is already assigned to this program"));
        }

        // Create and save the relationship
        Program_Workout programWorkout = new Program_Workout();
        programWorkout.setProgram(program);
        programWorkout.setWorkout(workout);
        programWorkout.setDay(request.getDay());
        programWorkout.setMuscle(workout.getPrimary_muscle());
        programWorkout.setReps(request.getReps());
        if (request.getSets() != null)
            programWorkout.setSets(request.getSets());
        if (request.getDuration() != null)
            programWorkout.setDuration(request.getDuration());

        programWorkoutRepository.save(programWorkout);

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Workout successfully assigned to program"));
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
    public ResponseEntity<?> updateAssignedWorkoutToProgram(AssignWorkoutToProgramRequest request) {
        Integer programId = request.getProgramId();
        Integer workoutId = request.getWorkoutId();

        Optional<Program> programOptional = programRepository.findById(programId);
        if (programOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Program not found"));
        }

        Optional<Workout> workoutOptional = workoutRepository.findById(workoutId);
        if (workoutOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Workout not found"));
        }

        Program program = programOptional.get();
        Workout workout = workoutOptional.get();

        Optional<Program_Workout> programWorkoutOpt = programWorkoutRepository
                .findByProgramAndWorkoutAndDay(program, workout, request.getDay());

        if (programWorkoutOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Workout not assigned to program"));
        }

        Program_Workout pw = programWorkoutOpt.get();

        if (request.getSets() != null && !request.getSets().equals(pw.getSets())) {
            pw.setSets(request.getSets());
        }

        if (request.getReps() != null && !request.getReps().equals(pw.getReps())) {
            pw.setReps(request.getReps());
        }

        if (request.getDuration() != null && !request.getDuration().equals(pw.getDuration())) {
            pw.setReps(request.getDuration());
        }

        programWorkoutRepository.save(pw);

        return ResponseEntity.ok(Map.of("message", "Workout assignment updated successfully"));
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
    public ResponseEntity<?> unAssignWorkoutFromProgram(AssignWorkoutToProgramRequest request) {
        Integer programId = request.getProgramId();
        Integer workoutId = request.getWorkoutId();

        Optional<Program> programOptional = programRepository.findById(programId);
        if (programOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Program not found"));
        }

        Optional<Workout> workoutOptional = workoutRepository.findById(workoutId);
        if (workoutOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Workout not found"));
        }

        Program program = programOptional.get();
        Workout workout = workoutOptional.get();

        Optional<Program_Workout> programWorkoutOpt =
                programWorkoutRepository.findByProgramAndWorkoutAndDay(program, workout, request.getDay());

        if (programWorkoutOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Workout not assigned to program"));
        }

        programWorkoutRepository.delete(programWorkoutOpt.get());

        return ResponseEntity.ok(Map.of("message", "Workout successfully removed from program"));
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
    public ResponseEntity<?> assignProgramToUser(AssignProgramToUserRequest request) {
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

        // Create and send notification
        Notification notification = new Notification();
        notification.setTitle("New Assignment");
        notification.setContent("New Program has been assigned to You:" + program.getTitle());
        notification.setCreatedAt(LocalDateTime.now());
        // Persist notification first
        notification = notificationRepository.save(notification); // Save and get managed instance

        notificationService.sendNotification(
                userOptional.get(),
                notification
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "program successfully assigned to user"));
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
    public ResponseEntity<?> unAssignProgramToUser(AssignProgramToUserRequest request) {
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

        // Create and send notification
        Notification notification = new Notification();
        notification.setTitle("Assignment Updated");
        notification.setContent("Program has been Unassigned from You:" + program.getTitle());
        notification.setCreatedAt(LocalDateTime.now());
        // Persist notification first
        notification = notificationRepository.save(notification); // Save and get managed instance

        notificationService.sendNotification(
                userOptional.get(),
                notification
        );

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
                    return new UserFeedBackResponse(user, User_Program.getFeedback());
                })
                .toList();

        ProgramResponse responseDto = new ProgramResponse(
                program.getId(),
                program.getTitle(),
                program.getLevel(),
                program.getIsPublic(),
                calculateRate(program.getId()),
                buildProgramScheduleResponse(program),
                feedBacks
        );
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }


    // Nael
    @PreAuthorize("hasAnyAuthority('User')")
    public ResponseEntity<?> getMyAssignedPrograms() {
        User user = HandleCurrentUserSession.getCurrentUser();

        List<User_Program> userPrograms = userProgramRepository.findByUser(user);

        if (userPrograms.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No Programms assigned to this user"));
        }

        List<ProgramResponse> programResponses = userPrograms.stream()
                .map(up -> {
                    Program program = up.getProgram();

                    return new ProgramResponse(
                            program.getId(),
                            program.getTitle(),
                            program.getLevel(),
                            program.getIsPublic(),
                            calculateRate(program.getId()),
                            buildProgramScheduleResponse(program),
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
                    .body(Map.of("message", "No Programs assigned to this user"));
        }

        List<ProgramResponse> programResponses = userPrograms.stream()
                .map(up -> {
                    Program program = up.getProgram();

                    return new ProgramResponse(
                            program.getId(),
                            program.getTitle(),
                            program.getLevel(),
                            program.getIsPublic(),
                            calculateRate(program.getId()),
                            buildProgramScheduleResponse(program),
                            null
                    );
                })
                .toList();

        return ResponseEntity.ok(programResponses);
    }


    private ProgramScheduleResponse buildProgramScheduleResponse(Program program) {
        Map<Day, WorkoutDayResponse> schedule = new LinkedHashMap<>();

        // Group all Program_Workout entries by day
        programWorkoutRepository.findByProgram(program)
                .stream()
                .collect(Collectors.groupingBy(Program_Workout::getDay))
                .forEach((day, dayWorkouts) -> {
                    // Group workouts by their primary muscle
                    Map<Muscle, List<WorkoutResponse>> groupedByMuscle = dayWorkouts.stream()
                            .collect(Collectors.groupingBy(
                                    Program_Workout::getMuscle,
                                    Collectors.mapping(pw -> new WorkoutResponse(
                                            pw.getWorkout().getId(),
                                            pw.getWorkout().getTitle(),
                                            pw.getWorkout().getAvg_calories(),
                                            pw.getWorkout().getPrimary_muscle().name(),
                                            String.join(", ", pw.getWorkout().getSecondary_muscles() != null ? pw.getWorkout().getSecondary_muscles().name() : null),
                                            pw.getWorkout().getAvg_calories() * pw.getSets() * pw.getDuration(),
                                            pw.getWorkout().getDescription(),
                                            pw.getWorkout().getImagePath(),
                                            pw.getReps(),
                                            pw.getSets(),
                                            pw.getDuration(),
                                            pw.getId()
                                    ), Collectors.toList())
                            ));

                    schedule.put(day, new WorkoutDayResponse(
                            groupedByMuscle.getOrDefault(Muscle.Chest, List.of()),
                            groupedByMuscle.getOrDefault(Muscle.Back, List.of()),
                            groupedByMuscle.getOrDefault(Muscle.Shoulders, List.of()),
                            groupedByMuscle.getOrDefault(Muscle.Biceps, List.of()),
                            groupedByMuscle.getOrDefault(Muscle.Triceps, List.of()),
                            groupedByMuscle.getOrDefault(Muscle.Forearms, List.of()),
                            groupedByMuscle.getOrDefault(Muscle.Abs, List.of()),
                            groupedByMuscle.getOrDefault(Muscle.Glutes, List.of()),
                            groupedByMuscle.getOrDefault(Muscle.Quadriceps, List.of()),
                            groupedByMuscle.getOrDefault(Muscle.Hamstrings, List.of()),
                            groupedByMuscle.getOrDefault(Muscle.Calves, List.of())
                    ));
                });

        return new ProgramScheduleResponse(schedule);
    }


    @PreAuthorize("hasAnyAuthority('User')")
    public ResponseEntity<?> checkIfUserInProgram(Integer programId) {
        var program = programRepository.findById(programId).orElse(null);
        if (program != null) {
            var isExist = userProgramRepository.existsByUserAndProgram(HandleCurrentUserSession.getCurrentUser(), program);
            return isExist ? ResponseEntity.status(HttpStatus.OK)
                    .body(Map.of("message", "user registered in this program")) :
                    ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("message", "user not registered in this program"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", "program with this id not found"));
    }



}