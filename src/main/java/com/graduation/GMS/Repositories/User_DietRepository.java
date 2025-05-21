package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.DietPlan;
import com.graduation.GMS.Models.User;
import com.graduation.GMS.Models.User_Diet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface User_DietRepository extends JpaRepository<User_Diet, Integer> {
    // Optimized exists query
    @Query("SELECT CASE WHEN COUNT(ud) > 0 THEN true ELSE false END " +
            "FROM User_Diet ud WHERE ud.user = :user AND ud.diet_plan = :diet_plan")
    boolean existsByUserAndDietPlan(@Param("user") User user, @Param("diet_plan") DietPlan diet_plan);

    // Paginated version available
    @Query("SELECT ud FROM User_Diet ud WHERE ud.diet_plan = :diet_plan")
    List<User_Diet> findByDietPlan(@Param("diet_plan") DietPlan diet_plan);

    // Optimized single record fetch
    @Query("SELECT ud FROM User_Diet ud " +
            "WHERE ud.user = :user AND ud.diet_plan = :diet_plan")
    Optional<User_Diet> findByUserAndDietPlan(
            @Param("user") User user,
            @Param("diet_plan") DietPlan diet_plan);

    // Explicit feedback query with non-null check
    @Query("SELECT ud FROM User_Diet ud " +
            "WHERE ud.diet_plan = :diet_plan AND ud.feedBack IS NOT NULL")
    List<User_Diet> findFeedbackByDietPlan(@Param("diet_plan") DietPlan diet_plan);

    // User lookup with pagination option
    @Query("SELECT ud FROM User_Diet ud WHERE ud.user = :user")
    List<User_Diet> findByUser(@Param("user") User user);
}