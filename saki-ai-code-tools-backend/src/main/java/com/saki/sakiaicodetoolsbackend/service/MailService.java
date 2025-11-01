package com.saki.sakiaicodetoolsbackend.service;

import com.saki.sakiaicodetoolsbackend.model.enums.MailTemplateEnum;
import jakarta.mail.MessagingException;

import java.io.IOException;
import java.util.Map;

/**
 * 邮件发送服务接口
 * 提供验证码类邮件发送能力
 * @author saki酱
 * @version 1.0
 * @since 2025-10-28
 */
public interface MailService {

    /**
     * 发送验证码邮件
     *
     * @param email     收件人邮箱
     * @param variables 参数内容，例如 code
     * @param type      模板类型
     */
    void sendEmailCode(String email, Map<String, Object> variables, MailTemplateEnum type) throws MessagingException, IOException;
}
