package com.saki.sakiaicodetoolsbackend.service.email.strategy.scene.impl;

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
 * 修改邮箱验证码策略
 * <p>场景：用户修改邮箱时发送验证码到新邮箱</p>
 *
 * Bean 名称必须为 "UPDATE_EMAIL"，以便工厂通过 scene 识别
 *
 * @author saki
 * @version 1.0
 * @since 2025-10-23
 */
@Slf4j
@Component("UPDATE_EMAIL")
public class UpdateEmailSceneStrategy extends AbstractVerificationSceneStrategy {

    public UpdateEmailSceneStrategy(StringRedisTemplate redisTemplate, VerificationChannelFactory factory, UserMapper userMapper) {
        super(redisTemplate, factory, userMapper);
    }

    @Override
    protected Map<String, Object> buildVariables(String code, SendCodeRequest request) {
        return Map.of();
    }

    @Override
    protected void preCheck(String receiver, String channel) {
        // 修改邮箱无需验证用户存在
    }

    @Override
    protected MailTemplateEnum getTemplateType() {
        return MailTemplateEnum.UPDATE_EMAIL_CODE;
    }

    @Override
    protected VerificationSceneEnum getScene() {
        return VerificationSceneEnum.UPDATE_EMAIL;
    }
}