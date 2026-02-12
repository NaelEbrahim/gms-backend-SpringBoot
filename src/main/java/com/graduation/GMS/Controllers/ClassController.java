package com.graduation.GMS.Controllers;

import com.graduation.GMS.DTO.Request.*;
import com.graduation.GMS.Services.ClassService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api/class")
@AllArgsConstructor
public class ClassController {

    private ClassService classService;

    // create a new class
    @PostMapping("/create")
    public ResponseEntity<?> createClass(@Valid @ModelAttribute CreateClassRequest classRequest) {
        return classService.createClass(classRequest);
    }

    // update an existing class
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateClass(@PathVariable Integer id, @Valid @RequestBody ClassRequest classRequest) {
        return classService.updateClass(id, classRequest);
    }

    // delete a class
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteClass(@PathVariable Integer id) {
        return classService.deleteClass(id);
    }

    @PutMapping("upload-image")
    public ResponseEntity<?> uploadClassImage(@ModelAttribute ImageRequest request) {
        return classService.uploadClassImage(request);
    }

    // get details of a specific class by ID
    @GetMapping("/show/{id}")
    public ResponseEntity<?> getClassById(@PathVariable Integer id) {
        return classService.getClassById(id);
    }

    // get all classes
    @GetMapping("/show/all")
    public ResponseEntity<?> getAllClasses(@RequestParam(required = false) Integer page,
                                           @RequestParam(required = false) Integer size) {
        if (page == null || size == null) {
            // No pagination
            return classService.getAllClasses(null);
        } else {
            Pageable pageable = PageRequest.of(page, size);
            return classService.getAllClasses(pageable);
        }
    }

    // assign a program to a class (Request body)
    @PostMapping("/assign-program")
    public ResponseEntity<?> assignProgramToClass(@Valid @RequestBody AssignProgramToClassRequest request) {
        return classService.assignProgramToClass(request);
    }

    // Unassign a program to a class (Request body)
    @PostMapping("/unassign-program")
    public ResponseEntity<?> unAssignProgramToClass(@Valid @RequestBody AssignProgramToClassRequest request) {
        System.out.println(request.getClassId() + "-" + request.getProgramId());
        return classService.unAssignProgramToClass(request);
    }

    // add Subscription
    @PostMapping("/new-subscription")
    public ResponseEntity<?> addNewSubscription(@Valid @RequestBody ClassSubscriptionRequest request) throws Exception {
        return classService.addNewSubscription(request);
    }

    // update Subscription
    @PostMapping("/update-subscription")
    public ResponseEntity<?> updateSubscription(@Valid @RequestBody ClassSubscriptionRequest request) throws Exception {
        return classService.updateSubscription(request);
    }

    // Get all subscribers for a class
    @GetMapping("/subscribers-by-class/{classId}")
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

    @GetMapping("/get-user-subscription-classes/{userId}")
    public ResponseEntity<?> getUserSubscriptionClasses(@PathVariable Integer userId) {
        return classService.getUserSubscriptionClasses(userId);
    }

    @PutMapping("/inActive-user-subscription")
    public ResponseEntity<?> inActiveSubscription(@RequestBody Map<String, String> body) {
        return classService.inActiveUserSubscription(Integer.valueOf(body.get("userId")), Integer.valueOf(body.get("classId")));
    }

    @DeleteMapping("/delete-user-feedback")
    public ResponseEntity<?> deleteUserFeedback(@RequestBody Map<String, String> data) {
        return classService.deleteClassFeedBack(Integer.valueOf(data.get("userId")), Integer.valueOf(data.get("classId")));
    }

    @GetMapping("/get-class-programs/{classId}")
    public ResponseEntity<?> getClassPrograms(@PathVariable Integer classId) {
        return classService.getClassPrograms(classId);
    }

    @GetMapping("/get-user-subscriptions-history/{userId}")
    public ResponseEntity<?> getUserSubscriptions(@PathVariable Integer userId) {
        return classService.getUserSubscriptions(userId);
    }

    @GetMapping("/get-class-subscriptions-history/{classId}")
    public ResponseEntity<?> getClassSubscriptions(@PathVariable Integer classId) {
        return classService.getUserSubscriptions(classId);
    }

    @GetMapping("/get-expired-subscriptions/{classId}")
    public ResponseEntity<?> getExpiredSubscriptionsByClassId(@PathVariable Integer classId) {
        return classService.getExpiredSubscriptionsByClassId(classId);
    }

}
