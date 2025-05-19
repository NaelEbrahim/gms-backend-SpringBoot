package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.Class;
import com.graduation.GMS.Models.Subscription;
import com.graduation.GMS.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {

    // Find a subscription by User and Class (using @Query for clarity)
    @Query("SELECT s FROM Subscription s WHERE s.user = :user AND s.aClass = :aClass")
    Optional<Subscription> findByUserAndAClass(
            @Param("user") User user,
            @Param("aClass") Class aClass
    );

    // Find all subscriptions for a given Class
    @Query("SELECT s FROM Subscription s WHERE s.aClass = :aClass")
    List<Subscription> findByAClass(@Param("aClass") Class aClass);

    // Find all subscriptions by active status
    @Query("SELECT s FROM Subscription s WHERE s.aClass = :aClass AND s.isActive = :isActive")
    List<Subscription> findByAClassAndIsActive(@Param("aClass") Class aClass ,@Param("isActive") boolean isActive);

    @Query("SELECT s FROM Subscription s WHERE  s.feedback IS NOT NULL")
    List<Subscription> findFeedback();

    @Query("SELECT s FROM Subscription s WHERE s.user = :user")
    List<Subscription> findByUser(@Param("user") User user);

    @Query("SELECT s FROM Subscription s WHERE s.aClass = :class AND s.feedback IS NOT NULL")
    List<Subscription> findFeedbackByClass(@Param("class") Class classEntity);
}
