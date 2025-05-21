package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.Program;
import com.graduation.GMS.Models.User;
import com.graduation.GMS.Models.User_Program;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface User_ProgramRepository extends JpaRepository<User_Program, Integer> {

    // Optimized exists query
    @Query("SELECT CASE WHEN COUNT(up) > 0 THEN true ELSE false END " +
            "FROM User_Program up WHERE up.user = :user AND up.program = :program")
    boolean existsByUserAndProgram(@Param("user") User user, @Param("program") Program program);

    // Paginated version available
    @Query("SELECT up FROM User_Program up WHERE up.program = :program")
    List<User_Program> findByProgram(@Param("program") Program program);

    // Optimized single record fetch
    @Query("SELECT up FROM User_Program up " +
            "WHERE up.user = :user AND up.program = :program")
    Optional<User_Program> findByUserAndProgram(
            @Param("user") User user,
            @Param("program") Program program);

    // Explicit feedback query with non-null check
    @Query("SELECT up FROM User_Program up " +
            "WHERE up.program = :program AND up.feedback IS NOT NULL")
    List<User_Program> findFeedbackByProgram(@Param("program") Program program);


    // User lookup with pagination option
    @Query("SELECT up FROM User_Program up WHERE up.user = :user")
    List<User_Program> findByUser(@Param("user") User user);

}