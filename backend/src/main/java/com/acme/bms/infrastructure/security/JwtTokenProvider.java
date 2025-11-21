package com.acme.bms.infrastructure.security;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

    private final Key jwtSecret;
    private final int jwtExpirationHours;

    public JwtTokenProvider(@Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-hours:24}") int expirationHours) {
        this.jwtSecret = Keys.hmacShaKeyFor(secret.getBytes());
        this.jwtExpirationHours = expirationHours;
    }

    public String generateToken(Long userId, String role) {
        Instant now = Instant.now();
        Instant expiration = now.plus(jwtExpirationHours, ChronoUnit.HOURS);

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("role", role)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .signWith(jwtSecret, SignatureAlgorithm.HS512)
                .compact();
    }
}
