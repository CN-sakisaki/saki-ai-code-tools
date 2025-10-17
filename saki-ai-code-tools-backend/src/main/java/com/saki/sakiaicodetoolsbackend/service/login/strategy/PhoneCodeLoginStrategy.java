package com.saki.sakiaicodetoolsbackend.service.login.strategy;

import com.saki.sakiaicodetoolsbackend.constant.AuthConstants;
import com.saki.sakiaicodetoolsbackend.mapper.UserMapper;
import com.saki.sakiaicodetoolsbackend.model.dto.LoginRequest;
import com.saki.sakiaicodetoolsbackend.model.enums.LoginTypeEnum;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 手机号验证码登录策略。
 * @author saki酱
 * @version 1.0
 * @since 2025-10-17 14:58
 */
@Component
public class PhoneCodeLoginStrategy extends AbstractCodeLoginStrategy {

    public PhoneCodeLoginStrategy(UserMapper userMapper, StringRedisTemplate stringRedisTemplate) {
        super(userMapper, stringRedisTemplate);
    }

    @Override
    public LoginTypeEnum getLoginType() {
        return LoginTypeEnum.PHONE_CODE;
    }

    @Override
    protected String getIdentifier(LoginRequest request) {
        return request.getUserPhone();
    }

    @Override
    protected String buildRedisKey(String identifier) {
        return AuthConstants.buildPhoneCodeKey(identifier);
    }

    @Override
    protected String missingIdentifierMessage() {
        return "手机号不能为空";
    }

    @Override
    protected String userNotFoundMessage() {
        return "手机号不存在";
    }

    @Override
    protected String getIdentifierColumn() {
        return "user_phone";
    }
}
