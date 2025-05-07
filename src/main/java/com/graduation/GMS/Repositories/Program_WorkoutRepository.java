package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.Program;
import com.graduation.GMS.Models.Program_Workout;
import com.graduation.GMS.Models.Workout;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Program_WorkoutRepository extends JpaRepository<Program_Workout, Integer> {
    boolean existsByProgramAndWorkout(Program program, Workout workout);
}
