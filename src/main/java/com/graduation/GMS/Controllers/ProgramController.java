package com.graduation.GMS.Controllers;

import com.graduation.GMS.DTO.Request.*;
import com.graduation.GMS.Services.ProgramService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/program")
@AllArgsConstructor
public class ProgramController {

    private ProgramService programService;

    @PostMapping("/create")
    public ResponseEntity<?> createProgram(@Valid @RequestBody ProgramRequest request) {
        return programService.createProgram(request);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateProgram(@PathVariable Integer id,
                                           @Valid @RequestBody ProgramRequest request) {
        return programService.updateProgram(id, request);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteProgram(@PathVariable Integer id) {
        return programService.deleteProgram(id);
    }

    @GetMapping("/show/{id}")
    public ResponseEntity<?> getProgramById(@PathVariable Integer id) {
        return programService.getProgramById(id);
    }

    @GetMapping("show/all")
    public ResponseEntity<?> getPrograms(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page == null || size == null) {
            // No pagination
            return programService.getAllPrograms(null);
        } else {
            Pageable pageable = PageRequest.of(page, size);
            return programService.getAllPrograms(pageable);
        }
    }

    @PostMapping("/assign-workout")
    public ResponseEntity<?> assignWorkoutToProgram(
            @Valid @RequestBody AssignWorkoutToProgramRequest request) {
        return programService.assignWorkoutToProgram(request);
    }

    @PostMapping("/update-assign-workout")
    public ResponseEntity<?> updateAssignWorkoutToProgram(@RequestBody @Valid AssignWorkoutToProgramRequest request) {
        return programService.updateAssignedWorkoutToProgram(request);
    }

    @PostMapping("/unassign-workout")
    public ResponseEntity<?> unAssignWorkoutToProgram(@RequestBody @Valid AssignWorkoutToProgramRequest request) {
        return programService.unAssignWorkoutFromProgram(request);
    }

    // Assign a program to a user
    @PostMapping("/assign")
    public ResponseEntity<?> assignProgramToUser(@RequestBody AssignProgramToUserRequest request) {
        return programService.assignProgramToUser(request);
    }

    // Unassign a program from a user
    @PostMapping("/unassign")
    public ResponseEntity<?> unAssignProgramFromUser(@RequestBody AssignProgramToUserRequest request) {
        return programService.unAssignProgramToUser(request);
    }

    // Rate a program
    @PostMapping("/rate")
    public ResponseEntity<?> rateProgram(@RequestBody RateProgramRequest request) {
        return programService.rateProgram(request);
    }

    // Submit feedback for a program
    @PostMapping("/feedback")
    public ResponseEntity<?> submitFeedback(@RequestBody FeedBackProgramRequest request) {
        return programService.submitFeedback(request);
    }

    // Get all feedbacks for a specific program
    @GetMapping("/{programId}/feedbacks")
    public ResponseEntity<?> getAllProgramFeedbacks(@PathVariable Integer programId) {
        return programService.getAllProgramFeedBacks(programId);
    }

    // Get all programs assigned to the currently logged-in user
    @GetMapping("/my-programs")
    public ResponseEntity<?> getMyAssignedPrograms() {
        return programService.getMyAssignedPrograms();
    }

    // Get all programs assigned to a specific user (Admin/Coach only)
    @GetMapping("/assigned/{userId}")
    public ResponseEntity<?> getAssignedProgramsByUserId(@PathVariable Integer userId) {
        return programService.getAssignedProgramsByUserId(userId);
    }

    @GetMapping("/checkUserInProgram/{programId}")
    public ResponseEntity<?> checkIfUserInProgram(@PathVariable Integer programId) {
        return programService.checkIfUserInProgram(programId);
    }

    @GetMapping("/get-members-in-program/{programId}")
    public ResponseEntity<?> getMembersInProgram(@PathVariable Integer programId) {
        return programService.getProgramSubscribers(programId);
    }

}