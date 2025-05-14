package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.Enums.Roles;
import com.graduation.GMS.Models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role,Integer> {

    Optional<Role> findByRoleName (Roles roleName);

}
