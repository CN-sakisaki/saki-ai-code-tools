package com.saki.sakiaicodetoolsbackend.service.email.strategy.channel.impl;

import com.saki.sakiaicodetoolsbackend.model.enums.MailTemplateEnum;
import com.saki.sakiaicodetoolsbackend.service.email.strategy.channel.AbstractVerificationChannelStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 短信验证码发送策略
 * 通过短信服务发送验证码信息
 *
 * @author saki酱
 * @version 1.0
 * @since 2025-10-24
 */
@Component("PHONE")
@Slf4j
public class PhoneChannelStrategy extends AbstractVerificationChannelStrategy {

    /**
     * 执行短信发送逻辑
     * 目前为模拟实现，后续可接入具体的短信服务商SDK
     *
     * @param receiver  手机号码
     * @param template  短信模板枚举
     * @param variables 模板变量（包含验证码、有效期等信息）
     */
    @Override
    protected void doSend(String receiver, MailTemplateEnum template, Map<String, Object> variables) {
        String code = String.valueOf(variables.get("code"));
        Integer expire = (Integer) variables.getOrDefault("expireMinutes", 5);
        // TODO: 调用阿里云 / 腾讯云短信SDK发送
        log.info("发送短信验证码 [{}] 至 [{}]，有效期 {} 分钟", code, receiver, expire);
    }

    /**
     * 获取短信渠道类型
     *
     * @return 渠道类型描述
     */
    @Override
    protected String getChannelType() {
        return "短信";
    }
}
