package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.Class;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClassRepository extends JpaRepository<Class,Integer> {
    Optional<Class> findByName(String name);
}
