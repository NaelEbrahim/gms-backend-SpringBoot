package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.Workout;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkoutRepository extends JpaRepository<Workout, Integer> {
    Optional<Workout> findByTitle(String title);
}
