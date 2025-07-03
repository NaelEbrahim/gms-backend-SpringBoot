package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.Session;
import com.graduation.GMS.Models.Session_Attendance;
import com.graduation.GMS.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface Session_AttendanceRepository extends JpaRepository<Session_Attendance,Integer> {
    boolean existsByUserAndDateBetween(User user, LocalDateTime startOfDay, LocalDateTime endOfDay);

    List<Session_Attendance> findTop30ByUserAndSessionOrderByDateDesc(User user, Session session);
}
