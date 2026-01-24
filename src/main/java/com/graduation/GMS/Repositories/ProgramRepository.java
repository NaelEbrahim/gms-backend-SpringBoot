package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.Program;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProgramRepository extends JpaRepository<Program, Integer> {
    Optional<Program> findByTitle(String title);

    @Query("SELECT p FROM Program p ORDER BY p.id")
    Page<Program> findAllPageable(Pageable pageable);
}
