package com.saki.sakiaicodetoolsbackend.service.login.strategy;

import com.saki.sakiaicodetoolsbackend.constant.AuthConstants;
import com.saki.sakiaicodetoolsbackend.mapper.UserMapper;
import com.saki.sakiaicodetoolsbackend.model.dto.login.LoginRequest;
import com.saki.sakiaicodetoolsbackend.model.enums.LoginTypeEnum;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 邮箱验证码登录策略。
 * @author saki酱
 * @version 1.0
 * @since 2025-10-17 12:02
 */
@Component
public class EmailCodeLoginStrategy extends AbstractCodeLoginStrategy {

    public EmailCodeLoginStrategy(UserMapper userMapper, StringRedisTemplate stringRedisTemplate) {
        super(userMapper, stringRedisTemplate);
    }

    @Override
    public LoginTypeEnum getLoginType() {
        return LoginTypeEnum.EMAIL_CODE;
    }

    @Override
    protected String getIdentifier(LoginRequest request) {
        return request.getUserEmail();
    }

    @Override
    protected String buildRedisKey(String identifier) {
        return AuthConstants.buildEmailCodeKey(identifier);
    }

    @Override
    protected String missingIdentifierMessage() {
        return "邮箱不能为空";
    }

    @Override
    protected String userNotFoundMessage() {
        return "邮箱不存在";
    }

    @Override
    protected String getIdentifierColumn() {
        return "user_email";
    }
}

