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
              WHERE w.primary_muscle = :muscle
            """)
    Page<Workout> findByPrimaryMuscle(
            @Param("muscle") Muscle muscle,
            Pageable pageable
    );

    @Query("SELECT w FROM Workout w ORDER BY w.id")
    Page<Workout> findAllPageable(Pageable pageable);

}
