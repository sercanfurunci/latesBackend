package com.example.senior_project.service;

import com.example.senior_project.config.JwtProperties;
import com.example.senior_project.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtProperties jwtProperties;

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_USER");

        log.debug("Generating token for user: {} with role: {}", userDetails.getUsername(), role);

        if (role.startsWith("ROLE_")) {
            role = role.substring(5);
            log.debug("Removed ROLE_ prefix: {}", role);
        }

        extraClaims.put("role", role);
        extraClaims.put("authorities", List.of(role));

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getExpirationMs()))
                .setIssuer(jwtProperties.getIssuer())
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            final String tokenRole = extractRole(token);
            final String userRole = userDetails.getAuthorities().stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .orElse(null);

            log.debug("Validating token for user: {}", username);
            log.debug("Token role: {}, User role: {}", tokenRole, userRole);

            String userRoleWithoutPrefix = userRole;
            if (userRole != null && userRole.startsWith("ROLE_")) {
                userRoleWithoutPrefix = userRole.substring(5);
            }

            return (username.equals(userDetails.getUsername())) &&
                    !isTokenExpired(token) &&
                    tokenRole != null &&
                    tokenRole.equals(userRoleWithoutPrefix);
        } catch (Exception e) {
            log.error("Error validating token: {}", e.getMessage(), e);
            return false;
        }
    }

    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (Exception e) {
            log.error("Error extracting username from token: {}", e.getMessage(), e);
            return null;
        }
    }

    public String extractRole(String token) {
        try {
            return extractClaim(token, claims -> claims.get("role", String.class));
        } catch (Exception e) {
            log.error("Error extracting role from token: {}", e.getMessage(), e);
            return null;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            log.error("Error checking token expiration: {}", e.getMessage(), e);
            return true;
        }
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }
}