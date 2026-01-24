package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Integer> {
    Optional<Session> findByTitle(String title);

    @Query("SELECT s FROM Session s ORDER BY s.id")
    Page<Session> findAllPageable(Pageable pageable);

    Page<Session> findAllByAClass_Id(Integer classId, Pageable pageable);

}
