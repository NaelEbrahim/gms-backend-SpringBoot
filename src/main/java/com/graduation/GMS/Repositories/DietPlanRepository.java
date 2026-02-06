package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.DietPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DietPlanRepository extends JpaRepository<DietPlan, Integer> {
    Optional<DietPlan> findByTitle(String title);

    @Query("SELECT d FROM DietPlan d ORDER BY d.id")
    Page<DietPlan> findAllPageable(Pageable pageable);

}
