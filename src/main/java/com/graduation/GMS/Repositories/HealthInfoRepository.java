package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.HealthInfo;
import com.graduation.GMS.Models.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface HealthInfoRepository extends JpaRepository<HealthInfo, Integer> {

    @Query("SELECT h FROM HealthInfo h WHERE h.user.id = :userId ORDER BY h.recordedAt DESC")
    List<HealthInfo> findLatestByUserId(@Param("userId") Integer userId, Pageable pageable);

    List<HealthInfo> findByUserOrderByRecordedAtDesc(User user);

}
