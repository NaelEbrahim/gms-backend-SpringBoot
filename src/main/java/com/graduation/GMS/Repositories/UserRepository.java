package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.Enums.Roles;
import com.graduation.GMS.Models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    @Query("SELECT ur.user FROM User_Role ur WHERE ur.role.roleName = :roleName")
    List<User> findAllByRoleName(@Param("roleName") Roles roleName);

    @Query("""
                SELECT DISTINCT u
                FROM User u
                JOIN u.userRoleList ur
                JOIN ur.role r
                WHERE r.roleName = :roleName
            """)
    Page<User> findUsersByRole(@Param("roleName") Roles roleName, Pageable pageable);

    @Query("SELECT u FROM User u ORDER BY u.id")
    Page<User> findAllUsers(Pageable pageable);

}
