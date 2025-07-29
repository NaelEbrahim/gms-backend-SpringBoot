package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.HealthInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HealthInfoRepository extends JpaRepository<HealthInfo, Integer> {

    List<HealthInfo> findByUserIdAndRecordedAtBetween(Integer userId,
                                                      LocalDate startDate,
                                                      LocalDate endDate);

    Optional<HealthInfo> findTopByUserIdOrderByRecordedAtDesc(Integer userId);

}
