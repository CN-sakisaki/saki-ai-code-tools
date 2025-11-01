package com.saki.sakiaicodetoolsbackend.service.email.strategy.scene.impl;

import com.saki.sakiaicodetoolsbackend.constant.AuthConstants;
import com.saki.sakiaicodetoolsbackend.mapper.UserMapper;
import com.saki.sakiaicodetoolsbackend.model.dto.common.SendCodeRequest;
import com.saki.sakiaicodetoolsbackend.model.enums.MailTemplateEnum;
import com.saki.sakiaicodetoolsbackend.model.enums.VerificationSceneEnum;
import com.saki.sakiaicodetoolsbackend.service.email.strategy.channel.VerificationChannelFactory;
import com.saki.sakiaicodetoolsbackend.service.email.strategy.scene.AbstractVerificationSceneStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 *
 * @author saki酱
 * @version 1.0
 * @since 2025-10-28
 */
@Component("REGISTER")
@Slf4j
public class RegisterSceneStrategy extends AbstractVerificationSceneStrategy {

    public RegisterSceneStrategy(StringRedisTemplate stringRedisTemplate, VerificationChannelFactory channelFactory, UserMapper userMapper) {
        super(stringRedisTemplate, channelFactory, userMapper);
    }

    @Override
    protected Map<String, Object> buildVariables(String code, SendCodeRequest request) {
        return Map.of(
                "code", code,
                "expireMinutes", AuthConstants.EMAIL_CODE_EXPIRE_MINUTES
        );
    }

    @Override
    protected void preCheck(String receiver, String channel) {
    }

    @Override
    protected MailTemplateEnum getTemplateType() {
        return MailTemplateEnum.REGISTER_CODE;
    }

    @Override
    protected VerificationSceneEnum getScene() {
        return VerificationSceneEnum.REGISTER;
    }
}
