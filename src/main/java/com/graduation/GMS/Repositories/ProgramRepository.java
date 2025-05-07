package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.Program;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.sql.ast.tree.expression.JdbcParameter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProgramRepository extends JpaRepository<Program, Integer> {
    Optional<Program> findByTitle(String title);
}
