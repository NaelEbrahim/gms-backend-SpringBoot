package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.Session;
import com.graduation.GMS.Models.User;
import com.graduation.GMS.Models.User_Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface User_SessionRepository extends JpaRepository<User_Session,Integer> {
    @Query("SELECT up FROM User_Session up WHERE up.session = :session")
    List<User_Session> findBySession(Session session);

    @Query("SELECT CASE WHEN COUNT(up) > 0 THEN true ELSE false END " +
            "FROM User_Session up WHERE up.user = :user AND up.session = :session")
    boolean existsByUserAndSession(User user, Session session);

    Optional<User_Session> findByUserAndSession(User user, Session session);

    @Query("SELECT up FROM User_Session up " +
            "WHERE up.session = :session AND up.feedback IS NOT NULL")
    List<User_Session> findFeedbackBySession(Session session);


    @Query("SELECT up FROM User_Session up WHERE up.user = :user")
    List<User_Session> findByUser(User user);

    @Query("SELECT COUNT(us) FROM User_Session us WHERE us.session = :session")
    int countBySession(@Param("session") Session session);

}
