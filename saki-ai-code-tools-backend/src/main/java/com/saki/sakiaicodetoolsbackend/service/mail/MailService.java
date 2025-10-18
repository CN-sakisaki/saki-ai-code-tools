package com.saki.sakiaicodetoolsbackend.service.mail;

import cn.hutool.core.util.StrUtil;
import com.saki.sakiaicodetoolsbackend.config.MailProperties;
import com.saki.sakiaicodetoolsbackend.constant.AuthConstants;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 发送邮件服务
 * @author saki酱
 * @version 1.0
 * @since 2025-10-18
 */
@Service
@Slf4j
public class MailService {

    // ===================== 常量定义 =====================

    /** 邮件主题 */
    private static final String EMAIL_SUBJECT = "登录验证码";

    /** 邮件模板路径 */
    private static final String EMAIL_TEMPLATE_PATH = "templates/login-code.html";

    // ===================== 依赖注入 =====================

    private final MailProperties mailProperties;

    /** 邮件发送器，用于发送验证码邮件 */
    private final JavaMailSender mailSender;

    /** 邮件模板缓存，使用原子引用保证线程安全 */
    private final AtomicReference<String> emailTemplateCache = new AtomicReference<>();

    public MailService(JavaMailSender mailSender, MailProperties mailProperties) {
        this.mailSender = mailSender;
        this.mailProperties = mailProperties;
    }

    /**
     * 发送验证码邮件。
     *
     * @param email 目标邮箱地址
     * @param code 验证码
     * @throws MessagingException 当邮件发送失败时抛出
     * @throws IOException 当邮件模板读取失败时抛出
     */
    public void sendEmailCode(String email, String code) throws MessagingException, IOException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());
        helper.setFrom(mailProperties.getMailFrom());
        helper.setTo(email);
        helper.setSubject(EMAIL_SUBJECT);
        // 构建邮件内容（HTML格式）
        helper.setText(buildEmailContent(code), true);
        mailSender.send(message);
    }

    /**
     * 构建邮件内容。
     * 使用模板文件或默认模板，替换验证码和过期时间占位符。
     *
     * @param code 验证码
     * @return 构建完成的邮件内容
     * @throws IOException 当模板文件读取失败时抛出
     */
    private String buildEmailContent(String code) throws IOException {
        // 使用缓存或加载邮件模板
        String template = emailTemplateCache.updateAndGet(current -> {
            if (StrUtil.isBlank(current)) {
                try {
                    return loadTemplate();
                } catch (IOException e) {
                    log.error("加载邮件模板失败", e);
                    return defaultTemplate();
                }
            }
            return current;
        });

        // 替换模板中的占位符
        return template.replace("${code}", code)
                .replace("${expireMinutes}", String.valueOf(AuthConstants.EMAIL_CODE_EXPIRE_MINUTES));
    }

    /**
     * 加载邮件模板文件。
     *
     * @return 模板文件内容
     * @throws IOException 当模板文件不存在或读取失败时抛出
     */
    private String loadTemplate() throws IOException {
        ClassPathResource resource = new ClassPathResource(EMAIL_TEMPLATE_PATH);
        if (!resource.exists()) {
            // 如果模板文件不存在，返回默认模板
            return defaultTemplate();
        }
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

    /**
     * 获取默认邮件模板。
     *
     * @return 默认模板内容
     */
    private String defaultTemplate() {
        return "<p>您的验证码为 <strong>${code}</strong>，有效期 ${expireMinutes} 分钟。</p>";
    }
}
