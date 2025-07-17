package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, Integer> {

    List<UserProgress> findByUserIdAndProgramWorkoutIdAndRecordedAtBetween(
            Integer userId,
            Integer programWorkoutId,
            LocalDate startDate,
            LocalDate endDate
    );

}

