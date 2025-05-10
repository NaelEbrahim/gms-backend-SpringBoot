package com.graduation.GMS.Services;


import com.graduation.GMS.Models.Enums.Roles;
import com.graduation.GMS.Models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private final String secret_Key = "d4XxeUg1xwh9UKQRUt51K8rz68M+UK1FCsKvMbHUmQ9dHCARrU99l0iqravNcEsN";

    public String generateJwt(User user, List<Roles> roles) {
        final long expiration_time = 86400000;
        Map<String, Object> claims = new HashMap<>();

        List<String> roleNames = roles.stream()
                .map(Enum::name)
                .collect(Collectors.toList());

        claims.put("roles", roleNames);

        return Jwts.builder()
                .claims(claims)
                .subject(user.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration_time))
                .signWith(Keys.hmacShaKeyFor(secret_Key.getBytes()))
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            var claims = getClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (JwtException ex) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secret_Key.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public List<Roles> extractRoles(String token) {
        Claims claims = getClaims(token);
        List<String> roleNames = claims.get("roles", List.class);
        return roleNames.stream()
                .map(Roles::valueOf)
                .collect(Collectors.toList());
    }


}
