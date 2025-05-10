package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.Workout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface WorkoutRepository extends JpaRepository<Workout, Integer> {
    Optional<Workout> findByTitle(String title);
}
