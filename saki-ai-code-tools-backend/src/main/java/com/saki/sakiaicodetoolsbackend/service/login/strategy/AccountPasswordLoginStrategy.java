package com.saki.sakiaicodetoolsbackend.service.login.strategy;

import com.saki.sakiaicodetoolsbackend.mapper.UserMapper;
import com.saki.sakiaicodetoolsbackend.model.dto.LoginRequest;
import com.saki.sakiaicodetoolsbackend.model.enums.LoginTypeEnum;
import org.springframework.stereotype.Component;

/**
 * 账号密码登录策略。
 * @author saki酱
 * @version 1.0
 * @since 2025-10-17 12:02
 */
@Component
public class AccountPasswordLoginStrategy extends AbstractPasswordLoginStrategy {

    public AccountPasswordLoginStrategy(UserMapper userMapper) {
        super(userMapper);
    }

    @Override
    public LoginTypeEnum getLoginType() {
        return LoginTypeEnum.ACCOUNT_PASSWORD;
    }

    @Override
    protected String getIdentifier(LoginRequest request) {
        return request.getUserAccount();
    }

    @Override
    protected String getIdentifierColumn() {
        return "user_account";
    }

    @Override
    protected String missingIdentifierMessage() {
        return "账号不能为空";
    }

    @Override
    protected String userNotFoundMessage() {
        return "账号不存在";
    }
}

