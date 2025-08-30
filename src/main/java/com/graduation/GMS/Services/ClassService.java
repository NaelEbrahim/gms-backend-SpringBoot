package com.graduation.GMS.Services;

import com.graduation.GMS.DTO.Request.*;
import com.graduation.GMS.DTO.Response.*;
import com.graduation.GMS.Models.*;
import com.graduation.GMS.Models.Class;
import com.graduation.GMS.Models.Enums.Day;
import com.graduation.GMS.Models.Enums.Muscle;
import com.graduation.GMS.Models.Enums.Roles;
import com.graduation.GMS.Repositories.*;
import com.graduation.GMS.Handlers.HandleCurrentUserSession;
import com.graduation.GMS.Services.GeneralServices.NotificationService;
import com.graduation.GMS.Tools.FilesManagement;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.graduation.GMS.DTO.Response.UserResponse.mapToUserResponse;

@Service
@AllArgsConstructor
public class ClassService {

    private UserRepository userRepository;

    private ClassRepository classRepository;

    private ProgramRepository programRepository;

    private Class_ProgramRepository class_ProgramRepository;

    private Program_WorkoutRepository programWorkoutRepository;

    private SubscriptionRepository subscriptionRepository;

    private SubscriptionHistoryRepository subscriptionHistoryRepository;

    private User_ProgramRepository userProgramRepository;

    private NotificationService notificationService;

    private NotificationRepository notificationRepository;


    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
    public ResponseEntity<?> createClass(CreateClassRequest request) {
        // Check if the class title already exists (optional validation)
        Optional<Class> existingClass = classRepository.findByName(request.getName());
        if (existingClass.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Class title already exists"));
        }
        // Convert the DTO to entity and save
        Class classEntity = new Class();
        Optional<User> userOptional = userRepository.findById(request.getCoachId());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        }

        classEntity.setAuditCoach(userOptional.get());
        classEntity.setName(request.getName());
        classEntity.setDescription(request.getDescription());
        classEntity.setPrice(request.getPrice());

        // Save the class to the database
        classRepository.save(classEntity);

        String imagePath = FilesManagement.upload(request.getImage(), classEntity.getId(), "Classes");
        if (imagePath == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Upload failed"));
        }
        classEntity.setImagePath(imagePath);
        classRepository.save(classEntity);

        // Create and send notification
        Notification notification = new Notification();
        notification.setTitle("New Class: " + classEntity.getName());
        notification.setContent("A new class has been published: " + classEntity.getName());
        notification.setCreatedAt(LocalDateTime.now());
        // Persist notification first
        notification = notificationRepository.save(notification); // Save and get managed instance
        List<User> usersWithUserRole = userRepository.findAllByRoleName(Roles.User);

        notificationService.sendNotificationToUsers(
                usersWithUserRole,
                notification
        );
        // Return the response with the saved class details
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Class created successfully"));
    }

    // Update an existing class
    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
    public ResponseEntity<?> updateClass(Integer id, ClassRequest request) {
        Optional<Class> optionalClass = classRepository.findById(id);
        if (optionalClass.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Class not found"));
        }

        Class existingClass = optionalClass.get();

        Optional<User> userOptional = userRepository.findById(request.getCoachId());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        }
        if (!existingClass.getAuditCoach().getId().equals(request.getCoachId())) {
            existingClass.setAuditCoach(userOptional.get());
        }

        if (!existingClass.getName().equals(request.getName()) && !request.getName().isEmpty()) {
            existingClass.setName(request.getName());
        }
        if (!existingClass.getDescription().equals(request.getDescription()) && !request.getDescription().isEmpty()) {
            existingClass.setDescription(request.getDescription());
        }
        if (!existingClass.getPrice().equals(request.getPrice()) && request.getPrice() != null) {
            existingClass.setPrice(request.getPrice());
        }
        classRepository.save(existingClass);

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Class updated successfully"));
    }

    // Delete a class
    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
    public ResponseEntity<?> deleteClass(Integer id) {
        if (!classRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Class not found"));
        }

        classRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Class deleted successfully"));
    }

    public ResponseEntity<?> uploadClassImage(ImageRequest request) {
        Optional<Class> classOptional = classRepository.findById(request.getId());
        if (classOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Class not found"));
        }

        String imagePath = FilesManagement.upload(request.getImage(), request.getId(), "classes");
        if (imagePath == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Upload failed"));
        }

        classOptional.get().setImagePath(imagePath);
        classRepository.save(classOptional.get());

        return ResponseEntity.ok(Map.of("message", "Class image uploaded", "imageUrl", imagePath));
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

        ClassResponse responseDto = new ClassResponse(
                coachResponse,
                classEntity.getId(),
                classEntity.getName(),
                classEntity.getDescription(),
                classEntity.getImagePath(),
                classEntity.getPrice(),
                programResponses,
                null,
                null
        );

        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
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

                    return new ClassResponse(
                            mapToUserResponse(classEntity.getAuditCoach()),
                            classEntity.getId(),
                            classEntity.getName(),
                            classEntity.getDescription(),
                            classEntity.getImagePath(),
                            classEntity.getPrice(),
                            programResponses,
                            null,
                            null
                    );
                })
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK).body(classResponses);
    }

    // Method to assign a program to a class

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
    public ResponseEntity<?> assignProgramToClass(AssignProgramToClassRequest request) {
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

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Coach')")
    public ResponseEntity<?> unAssignProgramToClass(AssignProgramToClassRequest request) {
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
        if (!exists) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Program is Not assigned to this class"));
        }

        Class_Program classProgram = class_ProgramRepository.findByAClassAndProgram(classEntity, programEntity);

        class_ProgramRepository.delete(classProgram);

        class_ProgramRepository.save(classProgram);

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Program successfully  Unassigned From class"));
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Secretary')")
    public ResponseEntity<?> addNewSubscription(ClassSubscriptionRequest request) throws Exception {
        Optional<Class> classOptional = classRepository.findById(request.getClassId());

        // Validate class and user existence
        if (classOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Class not found"));
        }
        Optional<User> userOptional = userRepository.findById(request.getUserId());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        }

        // Check if user already has an active subscription
        Optional<Subscription> existingSubscription = subscriptionRepository
                .findByUserAndAClass(userOptional.get(), classOptional.get());
        if (existingSubscription.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "User already has an active subscription to this class"));
        }
        // Record payment if provided
        processPayment(request, userOptional.get(), classOptional.get());

        // Create and save subscription
        Subscription subscription = new Subscription();
        subscription.setUser(userOptional.get());
        subscription.setAClass(classOptional.get());
        subscription.setJoinedAt(LocalDateTime.now());
        subscription.setIsActive(true);
        subscriptionRepository.save(subscription);

        // Create and send notification
        Notification notification = new Notification();
        notification.setTitle("New Subscription");
        notification.setContent("New Subscription has been made in Class:" + subscription.getAClass().getName());
        notification.setCreatedAt(LocalDateTime.now());
        // Persist notification first
        notification = notificationRepository.save(notification); // Save and get managed instance

        notificationService.sendNotification(
                userOptional.get(),
                notification
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Subscription created successfully"));
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Secretary')")
    public ResponseEntity<?> updateSubscription(ClassSubscriptionRequest request) throws Exception {
        Optional<Class> classOptional = classRepository.findById(request.getClassId());

        // Validate class and user existence
        if (classOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Class not found"));
        }
        Optional<User> userOptional = userRepository.findById(request.getUserId());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        }

        // Check if user already has an active subscription
        Optional<Subscription> existingSubscription = subscriptionRepository
                .findByUserAndAClass(userOptional.get(), classOptional.get());
        if (existingSubscription.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "User not Subscribed to this class"));
        }
        //is active
        existingSubscription.get().setIsActive(true);
        subscriptionRepository.save(existingSubscription.get());
        // Record payment if provided
        processPayment(request, userOptional.get(), classOptional.get());

        // Create and send notification
        Notification notification = new Notification();
        notification.setTitle("Update Subscription");
        notification.setContent("Update Subscription has been made in Class:" + existingSubscription.get().getAClass().getName());
        notification.setCreatedAt(LocalDateTime.now());
        // Persist notification first
        notification = notificationRepository.save(notification); // Save and get managed instance

        notificationService.sendNotification(
                userOptional.get(),
                notification
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Subscription updated successfully"));
    }

    private void processPayment(ClassSubscriptionRequest request, User user, Class classEntity) throws Exception {
        if (request.getPaymentAmount() != null && request.getPaymentAmount() > 0) {
            // Validate payment amount matches class price with discount
            float expectedAmount = calculateExpectedAmount(classEntity.getPrice(), request.getDiscountPercentage());

            if (Math.abs(request.getPaymentAmount() - expectedAmount) > 0.01f) {
                throw new Exception("Payment amount doesn't match expected value");
            }

            SubscriptionHistory history = new SubscriptionHistory();
            history.setUser(user);
            history.setAClass(classEntity);
            history.setPaymentDate(LocalDateTime.now());
            history.setPaymentAmount(request.getPaymentAmount());
            history.setDiscountPercentage(request.getDiscountPercentage());
            subscriptionHistoryRepository.save(history);

            // Create and send notification
            Notification notification = new Notification();
            notification.setTitle("Payment Acknowledgment");
            notification.setContent("Payment Successfully....to class:" + classEntity.getName());
            notification.setCreatedAt(LocalDateTime.now());
            // Persist notification first
            notification = notificationRepository.save(notification); // Save and get managed instance

            notificationService.sendNotification(
                    user,
                    notification
            );
        }
    }

    private float calculateExpectedAmount(float basePrice, Float discountPercentage) {
        return discountPercentage != null ?
                basePrice * (1 - discountPercentage / 100) :
                basePrice;
    }

    @PreAuthorize("hasAnyAuthority('Admin','Coach','Secretary')")
    public ResponseEntity<?> getSubscribersByClass(Integer classId) {
        Optional<Class> classOptional = classRepository.findById(classId);
        if (classOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Class not found"));
        }


        Class classEntity = classOptional.get();
        UserResponse coachResponse = mapToUserResponse(classEntity.getAuditCoach());
        // Get all subscribers for this class
        List<UserResponse> subscribers = subscriptionRepository.findByAClass(classEntity)
                .stream()
                .map(subscription -> {
                    User user = subscription.getUser();
                    return mapToUserResponse(user);
                })
                .toList();

        ClassResponse responseDto = new ClassResponse(
                coachResponse,
                classEntity.getId(),
                classEntity.getName(),
                classEntity.getDescription(),
                classEntity.getImagePath(),
                classEntity.getPrice(),
                null,
                subscribers,
                null
        );

        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @PreAuthorize("hasAnyAuthority('Admin','Coach','Secretary')")
    public ResponseEntity<?> getSubscribersByActiveStatus(Integer classId, boolean isActive) {
        Optional<Class> classOptional = classRepository.findById(classId);
        if (classOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Class not found"));
        }


        Class classEntity = classOptional.get();
        UserResponse coachResponse = mapToUserResponse(classEntity.getAuditCoach());
        // Get only active subscribers
        List<UserResponse> activeSubscribers = subscriptionRepository.findByAClassAndIsActive(classEntity, isActive)
                .stream()
                .map(subscription -> {
                    User user = subscription.getUser();
                    return mapToUserResponse(user);
                })
                .toList();

        ClassResponse responseDto = new ClassResponse(
                coachResponse,
                classEntity.getId(),
                classEntity.getName(),
                classEntity.getDescription(),
                classEntity.getImagePath(),
                classEntity.getPrice(),
                null,
                activeSubscribers,
                null
        );
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('User')")
    public ResponseEntity<?> getMyClasses() {
        User currentUser = HandleCurrentUserSession.getCurrentUser();
        List<Subscription> subscriptions = subscriptionRepository.findByUser(currentUser);

        if (subscriptions.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No class subscriptions found"));
        }

        // Preload all needed data in bulk to avoid N+1 queries
        List<Class> classes = subscriptions.stream()
                .map(Subscription::getAClass)
                .toList();

        List<ClassResponse> classResponses = subscriptions.stream()
                .map(subscription -> {
                    Class classEntity = subscription.getAClass();
                    return new ClassResponse(
                            mapToUserResponse(classEntity.getAuditCoach()),
                            classEntity.getId(),
                            classEntity.getName(),
                            classEntity.getDescription(),
                            classEntity.getImagePath(),
                            classEntity.getPrice(),
                            null,
                            null,  // subscribers
                            null  // feedback
                    );
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(classResponses);
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('User')")
    public ResponseEntity<?> updateClassFeedback(FeedBackClassRequest request) {
        User currentUser = HandleCurrentUserSession.getCurrentUser();
        Optional<Class> classOptional = classRepository.findById(request.getClassId());

        if (classOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Class not found"));
        }

        Optional<Subscription> subscription = subscriptionRepository.findByUserAndAClass(
                currentUser,
                classOptional.get()
        );

        if (subscription.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You are not subscribed to this class"));
        }

        subscription.get().setFeedback(request.getFeedback());
        subscriptionRepository.save(subscription.get());

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Feedback updated successfully"));
    }

    public ResponseEntity<?> getAllCassFeedBacks(Integer classId) {
        Optional<Class> classOptional = classRepository.findById(classId);
        if (classOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Class not found"));
        }

        Class classEntity = classOptional.get();
        UserResponse coachResponse = mapToUserResponse(classEntity.getAuditCoach());
        // Get only active subscribers
        List<UserFeedBackResponse> feedBacks = subscriptionRepository.findFeedbackByClass(classEntity)
                .stream()
                .map(subscription -> {
                    User user = subscription.getUser();
                    return new UserFeedBackResponse(user, subscription.getFeedback());
                })
                .toList();

        ClassResponse responseDto = new ClassResponse(
                coachResponse,
                classEntity.getId(),
                classEntity.getName(),
                classEntity.getDescription(),
                classEntity.getImagePath(),
                classEntity.getPrice(),
                null,
                null,
                feedBacks

        );
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
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
                                            pw.getWorkout().getAvg_calories() * pw.getSets(),
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

    @PreAuthorize("hasAnyAuthority('Admin','Coach','Secretary')")
    public ResponseEntity<?> getClassesSubscribersByUser(Integer userId) {
        var user = userRepository.findById(userId).orElse(null);
        if (user == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "user with this id not found"));
        List<Subscription> userSubscriptions = subscriptionRepository.findByUser(user);
        List<ClassResponse> subscriptionClasses = new ArrayList<>();
        if (!userSubscriptions.isEmpty())
            for (Subscription item : userSubscriptions)
                subscriptionClasses.add(ClassResponse.mapToClassResponse(item.getAClass()));
        return ResponseEntity.status(HttpStatus.OK).body(subscriptionClasses);
    }

    @PreAuthorize("hasAnyAuthority('Admin','Coach','Secretary')")
    public ResponseEntity<?> inActiveUserSubscription(Integer userId, Integer classId) {
        var user = userRepository.findById(userId).orElse(null);
        var aClass = classRepository.findById(classId).orElse(null);
        if (user == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "user with this id not found"));
        if (aClass == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "class with this id not found"));
        Subscription userSubscription = subscriptionRepository.findByUserAndAClass(user, aClass).orElse(null);
        if (userSubscription == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "user not subscribe in this class"));
        userSubscription.setIsActive(false);
        subscriptionRepository.save(userSubscription);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "subscription inActivated Successfully"));
    }

}
