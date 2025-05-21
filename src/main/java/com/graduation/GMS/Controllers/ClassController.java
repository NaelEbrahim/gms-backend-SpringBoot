package com.graduation.GMS.Controllers;

import com.graduation.GMS.DTO.Request.AssignProgramToClassRequest;
import com.graduation.GMS.DTO.Request.ClassRequest;
import com.graduation.GMS.DTO.Request.ClassSubscriptionRequest;
import com.graduation.GMS.DTO.Request.FeedBackClassRequest;
import com.graduation.GMS.Services.ClassService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/class")
@AllArgsConstructor
public class ClassController {

    private ClassService classService;

    // Endpoint to create a new class
    @PostMapping("/create")
    public ResponseEntity<?> createClass(@Valid @RequestBody ClassRequest classRequest) {
        return classService.createClass(classRequest);
    }

    // Endpoint to update an existing class
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateClass(@PathVariable Integer id, @Valid @RequestBody ClassRequest classRequest) {
        return classService.updateClass(id, classRequest);
    }

    // Endpoint to delete a class
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteClass(@PathVariable Integer id) {
        return classService.deleteClass(id);
    }

    // Endpoint to get details of a specific class by ID
    @GetMapping("/show/{id}")
    public ResponseEntity<?> getClassById(@PathVariable Integer id) {
        return classService.getClassById(id);
    }

    // Endpoint to get all classes
    @GetMapping("/show/all")
    public ResponseEntity<?> getAllClasses() {
        return classService.getAllClasses();
    }

    // Endpoint to assign a program to a class (Request body)
    @PostMapping("/assign-program")
    public ResponseEntity<?> assignProgramToClass(@RequestBody @Valid AssignProgramToClassRequest request) {
        return classService.assignProgramToClass(request);
    }
    // Endpoint to add Subscription
    @PostMapping("/new-subscription")
    public ResponseEntity<?> addNewSubscription(@RequestBody @Valid ClassSubscriptionRequest request) throws Exception {
        return classService.addNewSubscription(request);
    }
    // Endpoint to update Subscription
    @PostMapping("/update-subscription")
    public ResponseEntity<?> updateSubscription(@RequestBody @Valid ClassSubscriptionRequest request) throws Exception {
        return classService.updateSubscription(request);
    }
    // Get all subscribers for a class
    @GetMapping("/{classId}/subscribers-by-class")
    public ResponseEntity<?> getClassSubscribers(@PathVariable Integer classId) {
        return classService.getSubscribersByClass(classId);
    }

    // Get active/inactive subscribers for a class
    @GetMapping("/{classId}/subscribers")
    public ResponseEntity<?> getClassSubscribersByStatus(
            @PathVariable Integer classId,
            @RequestParam Boolean isActive) {
        return classService.getSubscribersByActiveStatus(classId, isActive);
    }
    // Endpoint to add feedback
    @PostMapping("/feedBack")
    public ResponseEntity<?> addFeedBack(@RequestBody FeedBackClassRequest request) throws Exception {
        return classService.updateClassFeedback(request);
    }
    // Get active/inactive subscribers for a class
    @GetMapping("/{classId}/feedbacks")
    public ResponseEntity<?> getClassFeedBacks(
            @PathVariable Integer classId) {
        return classService.getAllCassFeedBacks(classId);
    }
    // Get active/inactive subscribers for a class
    @GetMapping("/my-classes")
    public ResponseEntity<?> getMyClasses() {
        return classService.getMyClasses();
    }
}
