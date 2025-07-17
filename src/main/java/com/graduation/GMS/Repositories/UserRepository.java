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
    SELECT ur.user FROM User_Role ur
    WHERE ur.role.roleName = :roleName
      AND (
          LOWER(ur.user.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
          LOWER(ur.user.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
          LOWER(ur.user.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
      )""")
    Page<User> searchByRoleAndKeyword(@Param("roleName") Roles roleName,
                                      @Param("keyword") String keyword,
                                      Pageable pageable);


}
