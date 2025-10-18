package com.saki.sakiaicodetoolsbackend.aop;

import com.saki.sakiaicodetoolsbackend.context.UserContext;
import com.saki.sakiaicodetoolsbackend.mapper.UserMapper;
import com.saki.sakiaicodetoolsbackend.model.entity.User;
import com.saki.sakiaicodetoolsbackend.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * JWT 用户认证切面。
 * <p>
 * 该切面负责拦截所有 Controller 层的方法，自动解析 JWT Token 并设置用户上下文。
 * 在方法执行前，如果请求头中包含有效的 JWT Token，则解析 Token 获取用户信息并存入 {@link UserContext}；
 * 在方法执行后，自动清理用户上下文，避免内存泄漏。
 * </p>
 * @author saki酱
 * @version 1.0
 * @since 2025-10-18
 */
@Slf4j
@Aspect
@Component
public class JwtUserAspect {

    private final UserMapper userMapper;

    private final JwtUtils jwtUtils;

    /**
     * 构造函数。
     *
     * @param jwtUtils JWT 工具类，用于解析和验证 Token
     * @param userMapper 用户数据访问器，用于查询用户信息
     */
    public JwtUserAspect(JwtUtils jwtUtils, UserMapper userMapper) {
        this.jwtUtils = jwtUtils;
        this.userMapper = userMapper;
    }

    /**
     * 切点定义：拦截所有 Controller 层的方法。
     * <p>
     * 匹配 com.saki.sakiaicodetoolsbackend.controller 包及其子包下所有类的所有方法。
     * </p>
     */
    @Pointcut("execution(* com.saki.sakiaicodetoolsbackend.controller..*(..))")
    public void controllerMethods() {}

    /**
     * 环绕通知：处理 JWT Token 解析和用户上下文管理。
     * <p>
     * 该方法在 Controller 方法执行前后执行，主要功能包括：
     * <ol>
     *   <li>从请求头中提取 Authorization 字段</li>
     *   <li>解析 Bearer Token 并验证其有效性</li>
     *   <li>从 Token 中提取用户ID和角色信息</li>
     *   <li>查询完整的用户信息并设置到用户上下文中</li>
     *   <li>执行目标 Controller 方法</li>
     *   <li>最终清理用户上下文，确保线程安全</li>
     * </ol>
     * </p>
     *
     * @param joinPoint 连接点，代表被拦截的 Controller 方法
     * @return 目标方法的执行结果
     * @throws Throwable 如果目标方法执行过程中抛出异常
     */
    @Around("controllerMethods()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取当前 HTTP 请求
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        // 解析 Authorization 请求头
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                // 解析 JWT Token
                Claims claims = jwtUtils.parseToken(token);
                String userId = claims.getSubject();
                String userRole = claims.get("userRole", String.class);

                // 查询用户信息
                User user = userMapper.selectOneById(Long.valueOf(userId));

                // 保存当前用户上下文
                UserContext.setUser(user);
                log.debug("当前请求用户: id={}, role={}", userId, userRole);
            } catch (Exception e) {
                log.warn("JWT解析失败: {}", e.getMessage());
            }
        }

        try {
            // 执行目标方法
            return joinPoint.proceed();
        } finally {
            // 清理上下文
            UserContext.clear();
        }
    }
}
