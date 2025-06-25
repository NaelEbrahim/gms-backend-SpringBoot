package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.Attendance;
import com.graduation.GMS.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {
    boolean existsByUserAndDateBetween(User user, LocalDateTime start, LocalDateTime end);

    List<Attendance> findTop30ByUserOrderByDateDesc(User user);

}

