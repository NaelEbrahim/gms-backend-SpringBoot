package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.Enums.Muscle;
import com.graduation.GMS.Models.Workout;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkoutRepository extends JpaRepository<Workout, Integer> {
    Optional<Workout> findByTitle(String title);

    @Query("""
    SELECT w FROM Workout w
    WHERE (:muscle IS NULL OR w.primary_muscle = :muscle OR w.secondary_muscles = :muscle)
      AND (
          LOWER(w.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
      )""")
    Page<Workout> searchWorkoutsByMuscleAndKeyword(
            @Param("keyword") String keyword,
            @Param("muscle") Muscle muscle,
            Pageable pageable
    );

}
