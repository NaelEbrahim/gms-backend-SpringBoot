package com.graduation.GMS.Controllers;

import com.graduation.GMS.DTO.Request.ProgramRequest;
import com.graduation.GMS.DTO.Request.AssignWorkoutToProgramRequest;
import com.graduation.GMS.Services.ProgramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/program")
public class ProgramController {

    @Autowired
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
    public ResponseEntity<?> getAllPrograms() {
        return programService.getAllPrograms();
    }

    @PostMapping("/assign-workout")
    public ResponseEntity<?> assignWorkoutToProgram(
            @Valid @RequestBody AssignWorkoutToProgramRequest request) {
        return programService.assignWorkoutToProgram(request);
    }
}