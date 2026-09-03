package com.bloodbuddy.jwt;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey getSigningKey() {

        return Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }
//
//    public String generateToken(String username, Role role) {
//
//        return Jwts.builder()
//                .compact(email)
//                .claim("role", role.name())
//                .issuedAt(new Date())
//                .expiration(
//                        new Date(
//                                System.currentTimeMillis()
//                                        + expiration
//                        )
//                )
//                .signWith(getSigningKey())
//                .compact();
//    }
//
//    public String extractEmail(String token) {
//
//        return extractClaims(token)
//                .getSubject();
//    }
//
//    public String extractRole(String token) {
//
//        return extractClaims(token)
//                .get("role", String.class);
//    }
//
//    public boolean isTokenValid(
//            String token,
//            String email) {
//
//        String tokenEmail = extractEmail(token);
//
//        return tokenEmail.equals(email)
//                && !isTokenExpired(token);
//    }
//
//    private boolean isTokenExpired(String token) {
//
//        return extractClaims(token)
//                .getExpiration()
//                .before(new Date());
//    }
//
//    private Claims extractClaims(String token) {
//
//        return Jwts.parser()
//                .verifyWith(getSigningKey())
//                .build()
//                .parseSignedClaims(token)
//                .getPayload();
//    }
}