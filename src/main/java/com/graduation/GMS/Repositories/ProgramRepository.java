package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.Program;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProgramRepository extends JpaRepository<Program, Integer> {
    Optional<Program> findByTitle(String title);
}
