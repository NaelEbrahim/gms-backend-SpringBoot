package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.Class;
import com.graduation.GMS.Models.Class_Program;
import com.graduation.GMS.Models.Program;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Class_ProgramRepository extends JpaRepository<Class_Program, Integer> {
    @Query("SELECT COUNT(cp) > 0 FROM Class_Program cp WHERE cp.aClass = :aClass AND cp.program = :program")
    boolean existsByAClassAndProgram(@Param("aClass") Class aClass, @Param("program") Program program);

    @Query("SELECT cp FROM Class_Program cp WHERE cp.aClass = :aClass")
    List<Class_Program> findByAClass(@Param("aClass") Class aClass);
}
