package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.Meal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MealRepository extends JpaRepository<Meal, Integer> {
    Optional<Meal> findByTitle(String title);
}
