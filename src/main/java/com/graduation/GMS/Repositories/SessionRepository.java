package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session,Integer> {
    Optional<Session> findByTitle(String title);
}
