package com.example.project.security;

import com.example.project.enums.Role;
import com.example.project.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // Token oluştur
    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("id", user.getId())
                .claim("roles", user.getRoles()) // Role enum listesi
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Token’dan userId çek
    public Long extractUserId(String token) {
        return parseClaims(token).get("id", Long.class);
    }

    // Token’dan username çek
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    // Token’dan role listesi çek
    @SuppressWarnings("unchecked")
    public List<Role> extractRoles(String token) {
        Object rolesObj = parseClaims(token).get("roles");
        if (rolesObj == null) return List.of();

        return ((List<Object>) rolesObj).stream()
                .map(String::valueOf)
                .map(Role::valueOf)
                .collect(Collectors.toList());
    }

    // Token validasyon
    public boolean validateToken(String token, String username) {
        return extractUsername(token).equals(username) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException | IllegalArgumentException e) {
            throw new RuntimeException("JWT token is invalid or expired", e);
        }
    }
}
