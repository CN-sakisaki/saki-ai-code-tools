package com.saki.sakiaicodetoolsbackend.aop;

import cn.hutool.core.util.StrUtil;
import com.saki.sakiaicodetoolsbackend.constant.AuthConstants;
import com.saki.sakiaicodetoolsbackend.context.UserContext;
import com.saki.sakiaicodetoolsbackend.exception.BusinessException;
import com.saki.sakiaicodetoolsbackend.exception.ErrorCode;
import com.saki.sakiaicodetoolsbackend.mapper.UserMapper;
import com.saki.sakiaicodetoolsbackend.model.entity.User;
import com.saki.sakiaicodetoolsbackend.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
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
@Order(1)
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
    @Pointcut("execution(* com.saki.sakiaicodetoolsbackend.controller..*(..)) " +
            "&& !@annotation(com.saki.sakiaicodetoolsbackend.annotation.NoAuth)")
    public void controllerMethods() {
    }

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
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        try {
            String token = resolveToken(request);
            if (StrUtil.isBlank(token)) {
                throw new BusinessException(ErrorCode.TOKEN_INVALID, "未提供身份凭证");
            }

            // 这里会抛出 ExpiredJwtException
            Claims claims = jwtUtils.parseToken(token);

            String userId = claims.getSubject();
            String userRole = claims.get("userRole", String.class);

            User user = userMapper.selectOneById(Long.valueOf(userId));
            if (user == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在或已删除");
            }

            UserContext.setUser(user);
            log.debug("当前请求用户: id={}, role={}", userId, userRole);

            // 执行目标方法
            return joinPoint.proceed();

        } catch (ExpiredJwtException e) {
            log.warn("Token 已过期: {}", e.getMessage());
            throw new BusinessException(ErrorCode.LOGIN_EXPIRED);
        } catch (JwtException e) {
            log.warn("Token 无效: {}", e.getMessage());
            throw new BusinessException(ErrorCode.TOKEN_INVALID, "无效的身份凭证，请重新登录");
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Token 参数错误");
        } finally {
            UserContext.clear();
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (StrUtil.isNotBlank(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (StrUtil.equals(AuthConstants.ACCESS_TOKEN_COOKIE_NAME, cookie.getName())
                        && StrUtil.isNotBlank(cookie.getValue())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

}
