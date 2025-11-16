package com.company.retail.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Set;

@Component
public class JwtUtils {

    private static final String SECRET_KEY = "RetailSystemSuperSecureSecretKeyForJWT123456"; // 🔒 Change this in production
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 8; // 8 hours

    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    // 🟩 Generate JWT Token
    public String generateToken(String username, Set<String> roles) {
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 🟦 Extract Username
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    // 🟨 Extract Roles
    public Set<String> extractRoles(String token) {
        return Set.copyOf(parseClaims(token).get("roles", java.util.List.class));
    }

    // 🟥 Validate Token
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}