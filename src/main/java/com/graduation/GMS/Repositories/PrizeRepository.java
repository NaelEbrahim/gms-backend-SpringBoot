package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.Event;
import com.graduation.GMS.Models.Prize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrizeRepository extends JpaRepository<Prize, Integer> {
    @Query("SELECT DISTINCT p FROM Prize p WHERE p.event = :event")
    List<Prize> findByEvent(Event event);
}
