package com.company.retail.security;

import com.company.retail.user.UserModel;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private static final String SECRET_KEY =
            "your-very-long-secure-secret-key-for-jwt-signing-please-change-this";

    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 10; // 10 hours

    // ===============================================================
    // 🔥 Generate Token With All Fields Needed By React Frontend
    // ===============================================================
    public String generateToken(UserModel user) {
        Map<String, Object> claims = new HashMap<>();

        // Basic details
        claims.put("username", user.getUsername());
        claims.put("fullName", user.getFullName());

        // All roles as List<String>
        List<String> roleList = user.getRoles()
                .stream()
                .map(Enum::name)
                .collect(Collectors.toList());

        claims.put("roles", roleList);

        // Single primary role for easier frontend usage
        String primaryRole = roleList.isEmpty() ? null : roleList.get(0);
        claims.put("role", primaryRole);

        // User ID
        claims.put("userId", user.getUserId());

        // Shop ID (if user belongs to a shop)
        if (user.getShop() != null) {
            claims.put("shopId", user.getShop().getId());
        }

        // Build token
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // ===============================================================
    // 🔹 Extract username
    // ===============================================================
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // ===============================================================
    // 🔹 Extract roles as List<String>
    // ===============================================================
    public List<String> extractRoles(String token) {
        return extractAllClaims(token).get("roles", List.class);
    }

    // ===============================================================
    // 🔹 Validate token
    // ===============================================================
    public boolean isTokenValid(String token, String username) {
        return username.equals(extractUsername(token)) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}