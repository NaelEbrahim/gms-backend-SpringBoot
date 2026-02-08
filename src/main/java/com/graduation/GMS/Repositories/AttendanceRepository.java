package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.Attendance;
import com.graduation.GMS.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {

    boolean existsByUserAndDate(User user, LocalDate date);

    List<Attendance> findByUserAndDateBetween(User user, LocalDate start, LocalDate end);

    List<Attendance> findByDateBetween(LocalDate start, LocalDate end);

}

