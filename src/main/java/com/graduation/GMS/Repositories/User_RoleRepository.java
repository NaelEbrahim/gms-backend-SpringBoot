package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.User_Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public interface User_RoleRepository extends JpaRepository<User_Role,Integer> {

    List<User_Role> findByUserId(Integer userId);

    List<User_Role> findByRoleId(Integer id);
}
