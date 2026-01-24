package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.SubscriptionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionHistoryRepository extends JpaRepository<SubscriptionHistory, Integer> {

    @Query("""
                SELECT sh FROM SubscriptionHistory sh
                WHERE sh.paymentDate = (
                    SELECT MAX(s2.paymentDate)
                    FROM SubscriptionHistory s2
                    WHERE s2.user.id = sh.user.id
                )
            """)
    List<SubscriptionHistory> findLatestSubscriptions();

    List<SubscriptionHistory> findByUserId (Integer userId);

    List<SubscriptionHistory> findByaClassId(Integer classId);



}
