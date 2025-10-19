package com.saki.sakiaicodetoolsbackend.service;

import com.mybatisflex.core.service.IService;
import com.saki.sakiaicodetoolsbackend.model.dto.login.LoginRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.login.RegisterRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.login.TokenRefreshRequest;
import com.saki.sakiaicodetoolsbackend.model.entity.User;
import com.saki.sakiaicodetoolsbackend.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户表 服务层。
 *
 * @author saki酱
 * @since 2025-10-15
 */
public interface UserService extends IService<User> {
    /**
     * 登录。
     *
     * @param request 登录请求
     * @return 用户信息
     */
    UserVO login(LoginRequest request, HttpServletRequest httpServletRequest);

    /**
     * 发送邮箱登录验证码。
     *
     * @param request 登录请求
     */
    void sendEmailLoginCode(LoginRequest request);

    /**
     * 刷新 AccessToken。
     *
     * @param request 刷新请求
     * @return 新的 AccessToken
     */
    String refreshAccessToken(TokenRefreshRequest request);

    /**
     * 用户注册。
     *
     * @param request 注册请求
     * @return 新用户的主键 ID
     */
    Long register(RegisterRequest request);
}
