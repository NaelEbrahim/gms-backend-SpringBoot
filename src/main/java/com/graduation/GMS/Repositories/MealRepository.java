package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.Meal;
import com.graduation.GMS.Models.Program;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MealRepository extends JpaRepository<Meal, Integer> {
    Optional<Meal> findByTitle(String title);

    @Query("SELECT m FROM Meal m ORDER BY m.id")
    Page<Meal> findAllPageable(Pageable pageable);
}
