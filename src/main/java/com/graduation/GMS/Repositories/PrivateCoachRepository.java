package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.PrivateCoach;
import com.graduation.GMS.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PrivateCoachRepository extends JpaRepository<PrivateCoach, Integer> {
    boolean existsByUserAndCoach(User user, User coach);

    Optional<PrivateCoach> findByUserAndCoach(User user, User coach);
}
