package com.saki.sakiaicodetoolsbackend.service.login.strategy;

import com.saki.sakiaicodetoolsbackend.mapper.UserMapper;
import com.saki.sakiaicodetoolsbackend.model.dto.LoginRequest;
import com.saki.sakiaicodetoolsbackend.model.enums.LoginTypeEnum;
import org.springframework.stereotype.Component;

/**
 * 手机号密码登录策略。
 */
@Component
public class PhonePasswordLoginStrategy extends AbstractPasswordLoginStrategy {

    public PhonePasswordLoginStrategy(UserMapper userMapper) {
        super(userMapper);
    }

    @Override
    public LoginTypeEnum getLoginType() {
        return LoginTypeEnum.PHONE_PASSWORD;
    }

    @Override
    protected String getIdentifier(LoginRequest request) {
        return request.getUserPhone();
    }

    @Override
    protected String getIdentifierColumn() {
        return "user_phone";
    }

    @Override
    protected String missingIdentifierMessage() {
        return "手机号不能为空";
    }

    @Override
    protected String userNotFoundMessage() {
        return "手机号未注册";
    }
}

