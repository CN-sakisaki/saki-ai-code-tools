package com.saki.sakiaicodetoolsbackend.service.login;

import com.saki.sakiaicodetoolsbackend.model.dto.login.LoginRequest;
import com.saki.sakiaicodetoolsbackend.model.entity.User;
import com.saki.sakiaicodetoolsbackend.model.enums.LoginTypeEnum;

/**
 * 登录策略接口，定义了不同的登录方式策略。
 * <p>
 * 该接口用于实现各种登录方式（如账号密码登录、手机号验证码登录等），每种方式都有对应的认证逻辑。
 * 登录策略的具体实现类应当根据不同的登录方式来实现 {@link #getLoginType()} 和 {@link #authenticate(LoginRequest)} 方法。
 * </p>
 *
 * @author saki酱
 * @version 1.0
 * @since 2025-10-17 12:02
 */
public interface LoginStrategy {

    /**
     * 策略支持的登录类型。
     *
     * @return 登录类型
     */
    LoginTypeEnum getLoginType();

    /**
     * 执行具体的认证逻辑。
     *
     * @param request 登录请求
     * @return 已通过认证的用户
     */
    User authenticate(LoginRequest request);
}

