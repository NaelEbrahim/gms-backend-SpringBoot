package com.graduation.GMS.Services;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    private final String secret_Key = "d4XxeUg1xwh9UKQRUt51K8rz68M+UK1FCsKvMbHUmQ9dHCARrU99l0iqravNcEsN";

    public String generateJwt(String email) {
        final long expiration_time = 86400;

        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * expiration_time))
                .signWith(Keys.hmacShaKeyFor(secret_Key.getBytes()))
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            var claims = getClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (JwtException ex) {
            System.out.println(ex.getMessage());
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

    public String getEmailFromToken (String token){
        return getClaims(token).getSubject();
    }


}
