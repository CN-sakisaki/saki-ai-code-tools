package com.saki.sakiaicodetoolsbackend.service.email.strategy.channel;

import com.saki.sakiaicodetoolsbackend.exception.BusinessException;
import com.saki.sakiaicodetoolsbackend.model.enums.MailTemplateEnum;

import java.util.Map;

/**
 * 验证码发送渠道策略接口（邮箱、短信等）
 * @author saki酱
 * @version 1.0
 * @since 2025-10-23
 */
public interface VerificationChannelStrategy {
    /**
     * 发送验证码（模板方法）
     * 提供统一的发送流程：执行发送、记录日志、异常处理
     *
     * @param receiver  接收者
     * @param mailTemplateEnum  邮件模板枚举
     * @param variables 模板变量
     * @throws BusinessException 当发送失败时抛出业务异常
     */
    void send(String receiver, MailTemplateEnum mailTemplateEnum, Map<String, Object> variables);
}
