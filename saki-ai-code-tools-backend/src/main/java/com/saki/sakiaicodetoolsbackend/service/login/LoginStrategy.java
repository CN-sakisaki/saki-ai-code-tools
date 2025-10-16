package com.saki.sakiaicodetoolsbackend.service.login;

import com.saki.sakiaicodetoolsbackend.model.dto.LoginRequest;
import com.saki.sakiaicodetoolsbackend.model.entity.User;
import com.saki.sakiaicodetoolsbackend.model.enums.LoginTypeEnum;

/**
 * 登录策略接口。
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

