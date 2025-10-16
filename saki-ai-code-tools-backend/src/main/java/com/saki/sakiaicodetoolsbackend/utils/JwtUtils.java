package com.saki.sakiaicodetoolsbackend.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类。
 */
@Component
public class JwtUtils {

    @Value("${jwt.secret:saki-ai-code-tools-default-secret}")
    private String secret;

    @Value("${jwt.access-expire-minutes:30}")
    private long accessExpireMinutes;

    @Value("${jwt.refresh-expire-days:7}")
    private long refreshExpireDays;

    public String generateAccessToken(Long userId, String userRole) {
        return generateToken(userId, userRole, getAccessTokenExpireMillis());
    }

    public String generateRefreshToken(Long userId, String userRole) {
        return generateToken(userId, userRole, getRefreshTokenExpireMillis());
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret.getBytes(StandardCharsets.UTF_8))
                .parseClaimsJws(token)
                .getBody();
    }

    public Claims parseTokenAllowExpired(String token) {
        try {
            return parseToken(token);
        } catch (ExpiredJwtException ex) {
            return ex.getClaims();
        }
    }

    public boolean isTokenValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public long getAccessTokenExpireMillis() {
        return Duration.ofMinutes(accessExpireMinutes).toMillis();
    }

    public long getRefreshTokenExpireMillis() {
        return Duration.ofDays(refreshExpireDays).toMillis();
    }

    public Duration getRefreshTokenExpireDuration() {
        return Duration.ofMillis(getRefreshTokenExpireMillis());
    }

    private String generateToken(Long userId, String userRole, long expireMillis) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expireMillis);
        Map<String, Object> claims = new HashMap<>(2);
        claims.put("userRole", userRole);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(SignatureAlgorithm.HS256, secret.getBytes(StandardCharsets.UTF_8))
                .compact();
    }
}

