package com.saki.sakiaicodetoolsbackend.service.email.strategy.scene.impl;

import com.saki.sakiaicodetoolsbackend.constant.AuthConstants;
import com.saki.sakiaicodetoolsbackend.exception.ErrorCode;
import com.saki.sakiaicodetoolsbackend.exception.ThrowUtils;
import com.saki.sakiaicodetoolsbackend.mapper.UserMapper;
import com.saki.sakiaicodetoolsbackend.model.dto.common.SendCodeRequest;
import com.saki.sakiaicodetoolsbackend.model.entity.User;
import com.saki.sakiaicodetoolsbackend.model.enums.MailTemplateEnum;
import com.saki.sakiaicodetoolsbackend.model.enums.VerificationSceneEnum;
import com.saki.sakiaicodetoolsbackend.service.email.strategy.channel.VerificationChannelFactory;
import com.saki.sakiaicodetoolsbackend.service.email.strategy.scene.AbstractVerificationSceneStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 登录验证码策略（支持邮箱 / 手机）
 * @author saki酱
 * @version 1.0
 * @since 2025-10-23
 */
@Component("LOGIN")
@Slf4j
public class LoginSceneStrategy extends AbstractVerificationSceneStrategy {

    public LoginSceneStrategy(UserMapper userMapper, StringRedisTemplate redisTemplate, VerificationChannelFactory factory) {
        super(redisTemplate, factory, userMapper);
    }

    @Override
    protected void preCheck(String receiver, String channel) {
        User user = selectByReceiver(receiver, channel);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在或未注册");
    }

    @Override
    protected Map<String, Object> buildVariables(String code, SendCodeRequest request) {
        User user = selectByReceiver(request.getReceiver(), request.getChannel());
        return Map.of(
                "code", code,
                "expireMinutes", AuthConstants.EMAIL_CODE_EXPIRE_MINUTES,
                "username", user.getUserName()
        );
    }

    @Override
    protected MailTemplateEnum getTemplateType() {
        return MailTemplateEnum.LOGIN_CODE;
    }

    @Override
    protected VerificationSceneEnum getScene() {
        return VerificationSceneEnum.LOGIN;
    }
}
