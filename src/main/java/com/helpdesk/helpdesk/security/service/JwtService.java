package com.helpdesk.helpdesk.security.service;

import com.helpdesk.helpdesk.user.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import com.helpdesk.helpdesk.user.enums.UserRole;
import java.util.UUID;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-expiration}") long accessTokenExpiration) {

        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessTokenExpiration = accessTokenExpiration;
    }

    public String generateAccessToken(User user) {

        Instant now = Instant.now();
        Instant expiration = now.plusMillis(accessTokenExpiration);

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    private Claims extractAllClaims(String token) {

        Jws<Claims> claimsJws = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);

        return claimsJws.getPayload();
    }

    public String generateAccessToken(UUID userId, UserRole role) {

        Instant now = Instant.now();
        Instant expiration = now.plusMillis(accessTokenExpiration);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public boolean isTokenValid(String token) {

        try {
            extractAllClaims(token);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpiration / 1000;
    }

    public String extractUserId(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public Instant extractExpiration(String token) {
        return extractAllClaims(token).getExpiration().toInstant();
    }
}