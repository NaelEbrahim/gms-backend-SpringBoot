package com.graduation.GMS.Controllers;

import com.graduation.GMS.DTO.Request.AssignProgramToClassRequest;
import com.graduation.GMS.DTO.Request.ClassRequest;
import com.graduation.GMS.Services.ClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/class")
public class ClassController {

    @Autowired
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
    @GetMapping("show/all")
    public ResponseEntity<?> getAllClasses() {
        return classService.getAllClasses();
    }

    // Endpoint to assign a program to a class (Request body)
    @PostMapping("/assign-program")
    public ResponseEntity<?> assignProgramToClass(@RequestBody @Valid AssignProgramToClassRequest request) {
        return classService.assignProgramToClass(request);
    }
}
