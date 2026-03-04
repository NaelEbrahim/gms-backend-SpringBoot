package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.Class;
import com.graduation.GMS.Models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassRepository extends JpaRepository<Class,Integer> {
    Optional<Class> findByName(String name);

    List<Class> findByAuditCoach(User coach);

    @Query("SELECT c FROM Class c ORDER BY c.id")
    Page<Class> findAllPageable (Pageable pageable);
}
