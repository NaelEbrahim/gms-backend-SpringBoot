package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.Class;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClassRepository extends JpaRepository<Class,Integer> {
    Optional<Class> findByName(String name);
}
