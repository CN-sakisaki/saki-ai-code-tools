package com.saki.sakiaicodetoolsbackend.service.email.strategy.channel.impl;

import com.saki.sakiaicodetoolsbackend.model.enums.MailTemplateEnum;
import com.saki.sakiaicodetoolsbackend.service.MailService;
import com.saki.sakiaicodetoolsbackend.service.email.strategy.channel.AbstractVerificationChannelStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 邮件验证码发送策略
 * 通过邮件服务发送验证码信息
 *
 * @author saki酱
 * @version 1.0
 * @since 2025-10-24
 */
@Component("EMAIL")
@RequiredArgsConstructor
public class EmailChannelStrategy extends AbstractVerificationChannelStrategy {

    /**
     * 邮件服务
     */
    private final MailService mailService;

    /**
     * 执行邮件发送逻辑
     *
     * @param receiver  邮件接收地址
     * @param template  邮件模板枚举
     * @param variables 模板变量（包含验证码等信息）
     * @throws Exception 邮件发送过程中可能出现的异常
     */
    @Override
    protected void doSend(String receiver, MailTemplateEnum template, Map<String, Object> variables) throws Exception {
        mailService.sendEmailCode(receiver, variables, template);
    }

    /**
     * 获取邮件渠道类型
     *
     * @return 渠道类型描述
     */
    @Override
    protected String getChannelType() {
        return "邮件";
    }
}
