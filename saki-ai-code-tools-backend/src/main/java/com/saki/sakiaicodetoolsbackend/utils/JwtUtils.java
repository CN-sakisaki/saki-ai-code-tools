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
 * JWT 工具类，用于生成、解析和验证JSON Web Tokens。
 * 支持访问令牌和刷新令牌的生成与管理。
 *
 * @author saki酱
 * @version 1.0
 * @since 2025-10-17 12:12
 */
@Component
public class JwtUtils {

    @Value("${jwt.secret:saki-ai-code-tools-default-secret}")
    private String secret;

    @Value("${jwt.access-expire-minutes:60}")
    private long accessExpireMinutes;

    @Value("${jwt.refresh-expire-days:7}")
    private long refreshExpireDays;

    /**
     * 生成访问令牌。
     *
     * @param userId 用户ID，不能为null
     * @param userRole 用户角色，不能为null或空
     * @return 生成的JWT访问令牌字符串
     * @throws IllegalArgumentException 如果userId或userRole为null
     */
    public String generateAccessToken(Long userId, String userRole) {
        return generateToken(userId, userRole, getAccessTokenExpireMillis());
    }

    /**
     * 生成刷新令牌。
     *
     * @param userId 用户ID，不能为null
     * @param userRole 用户角色，不能为null或空
     * @return 生成的JWT刷新令牌字符串
     * @throws IllegalArgumentException 如果userId或userRole为null
     */

    public String generateRefreshToken(Long userId, String userRole) {
        return generateToken(userId, userRole, getRefreshTokenExpireMillis());
    }

    /**
     * 解析JWT令牌并返回声明(Claims)。
     *
     * @param token JWT令牌字符串，不能为null或空
     * @return 包含令牌声明的Claims对象
     * @throws ExpiredJwtException 如果令牌已过期
     * @throws JwtException 如果令牌无效或签名验证失败
     * @throws IllegalArgumentException 如果token为null或空
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret.getBytes(StandardCharsets.UTF_8))
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 解析JWT令牌，即使令牌已过期也返回声明。
     * 主要用于处理过期令牌的场景，如刷新令牌时。
     *
     * @param token JWT令牌字符串，不能为null或空
     * @return 包含令牌声明的Claims对象，即使令牌已过期
     * @throws JwtException 如果令牌格式错误或签名验证失败（过期除外）
     * @throws IllegalArgumentException 如果token为null或空
     */
    public Claims parseTokenAllowExpired(String token) {
        try {
            return parseToken(token);
        } catch (ExpiredJwtException ex) {
            return ex.getClaims();
        }
    }

    /**
     * 验证JWT令牌是否有效。
     *
     * @param token JWT令牌字符串，不能为null或空
     * @return 如果令牌有效且未过期返回true，否则返回false
     */
    public boolean isTokenValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    /**
     * 获取访问令牌的过期时间（毫秒）。
     *
     * @return 访问令牌的过期时间（毫秒）
     */
    public long getAccessTokenExpireMillis() {
        return Duration.ofMinutes(accessExpireMinutes).toMillis();
    }

    /**
     * 获取刷新令牌的过期时间（毫秒）。
     *
     * @return 刷新令牌的过期时间（毫秒）
     */
    public long getRefreshTokenExpireMillis() {
        return Duration.ofDays(refreshExpireDays).toMillis();
    }

    /**
     * 获取刷新令牌的过期时长。
     *
     * @return 刷新令牌的过期时长Duration对象
     */
    public Duration getRefreshTokenExpireDuration() {
        return Duration.ofMillis(getRefreshTokenExpireMillis());
    }

    /**
     * 生成JWT令牌的私有方法。
     *
     * @param userId 用户ID，不能为null
     * @param userRole 用户角色，不能为null或空
     * @param expireMillis 令牌过期时间（毫秒）
     * @return 生成的JWT令牌字符串
     * @throws IllegalArgumentException 如果userId或userRole为null
     */
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