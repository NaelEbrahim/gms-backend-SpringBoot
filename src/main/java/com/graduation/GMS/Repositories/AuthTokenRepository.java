package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken, Integer> {

    void deleteByUserId(Integer userId);

    Optional<AuthToken> findByAccessToken (String accessToken);

}
