package com.saki.sakiaicodetoolsbackend.service.email.strategy.channel;

import com.saki.sakiaicodetoolsbackend.exception.BusinessException;
import com.saki.sakiaicodetoolsbackend.exception.ErrorCode;
import com.saki.sakiaicodetoolsbackend.model.enums.MailTemplateEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 验证渠道策略抽象基类
 * 提供统一的发送流程和异常处理，子类只需实现具体地发送逻辑
 *
 * @author saki酱
 * @version 1.0
 * @since 2025-10-28
 */
@Slf4j
public abstract class AbstractVerificationChannelStrategy implements VerificationChannelStrategy {

    /**
     * 发送验证码（模板方法）
     * 提供统一的发送流程：执行发送、记录日志、异常处理
     *
     * @param receiver  接收者
     * @param template  邮件模板枚举
     * @param variables 模板变量
     * @throws BusinessException 当发送失败时抛出业务异常
     */
    @Override
    public void send(String receiver, MailTemplateEnum template, Map<String, Object> variables) {
        try {
            doSend(receiver, template, variables);
            log.info("[{}] 模板 [{}] 已发送至 [{}]，变量：{}", getChannelType(), template.name(), receiver, variables);
        } catch (Exception e) {
            log.error("[{}] 渠道发送失败: {}", getChannelType(), e.getMessage(), e);
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAILED, String.format("%s 渠道发送失败，请稍后重试", getChannelType()));
        }
    }

    /**
     * 执行具体地发送逻辑
     * 由子类实现具体的渠道发送逻辑
     *
     * @param receiver  接收者
     * @param template  邮件模板枚举
     * @param variables 模板变量
     * @throws Exception 发送过程中可能出现的异常
     */
    protected abstract void doSend(String receiver, MailTemplateEnum template, Map<String, Object> variables) throws Exception;

    /**
     * 获取渠道类型
     * 用于日志记录和异常信息显示
     *
     * @return 渠道类型描述
     */
    protected abstract String getChannelType();
}