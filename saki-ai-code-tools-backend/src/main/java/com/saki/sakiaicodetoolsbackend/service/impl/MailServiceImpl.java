package com.saki.sakiaicodetoolsbackend.service.impl;

import cn.hutool.core.util.StrUtil;
import com.saki.sakiaicodetoolsbackend.config.CustomMailProperties;
import com.saki.sakiaicodetoolsbackend.constant.AuthConstants;
import com.saki.sakiaicodetoolsbackend.model.enums.MailTemplateEnum;
import com.saki.sakiaicodetoolsbackend.service.MailService;
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
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 邮件发送服务实现类
 * 提供通用的验证码邮件发送功能，支持多种邮件模板类型
 * 使用模板缓存机制提升性能，支持UTF-8编码
 *
 * @author saki酱
 * @version 1.1
 * @since 2025-10-23
 */
@Service
@Slf4j
public class MailServiceImpl implements MailService {

    /** 通用邮件模板路径 - 从classpath加载HTML模板文件 */
    private static final String UNIVERSAL_TEMPLATE_PATH = "templates/universal-code.html";

    /** Spring邮件发送器 - 负责实际的邮件发送 */
    private final JavaMailSender mailSender;

    /** 邮件配置属性 - 包含发件人地址等配置信息 */
    private final CustomMailProperties customMailProperties;

    /** 模板内容缓存 - 使用AtomicReference保证线程安全，避免重复读取模板文件 */
    private final AtomicReference<String> emailTemplateCache = new AtomicReference<>();

    /**
     * 构造函数
     *
     * @param mailSender 邮件发送器
     * @param customMailProperties 邮件配置属性
     */
    public MailServiceImpl(JavaMailSender mailSender, CustomMailProperties customMailProperties) {
        this.mailSender = mailSender;
        this.customMailProperties = customMailProperties;
    }

    /**
     * 发送邮件验证码
     * 根据模板类型和变量构建邮件内容并发送
     *
     * @param email 收件人邮箱地址
     * @param variables 模板变量（必须包含code字段）
     * @param type 邮件模板类型枚举
     * @throws MessagingException 当邮件构建或发送失败时抛出
     * @throws IOException 当模板文件读取失败时抛出
     */
    @Override
    public void sendEmailCode(String email, Map<String, Object> variables, MailTemplateEnum type) throws MessagingException, IOException {
        // 创建MIME邮件消息，支持HTML内容
        MimeMessage message = mailSender.createMimeMessage();
        // 使用UTF-8编码，支持中文
        MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());

        // 从变量中提取验证码
        String code = (String) variables.get("code");

        // 设置邮件基本信息
        helper.setFrom(customMailProperties.getMailFrom());
        helper.setTo(email);
        helper.setSubject(type.getSubject());
        // 设置为HTML格式的邮件内容
        helper.setText(buildEmailContent(code, type), true);

        // 发送邮件
        mailSender.send(message);
        log.info(" 已向 [{}] 发送 {} 邮件", email, type.getAction());
    }

    /**
     * 构建邮件HTML内容
     * 使用缓存机制提升模板加载性能，支持变量替换
     *
     * @param code 验证码
     * @param type 邮件模板类型
     * @return 构建完成的邮件HTML内容
     * @throws IOException 当模板文件读取失败时抛出
     */
    private String buildEmailContent(String code, MailTemplateEnum type) throws IOException {
        // 使用原子引用确保线程安全的模板缓存
        String template = emailTemplateCache.updateAndGet(current -> {
            // 如果缓存为空，则加载模板
            if (StrUtil.isBlank(current)) {
                try {
                    return loadTemplate();
                } catch (IOException e) {
                    log.error("加载邮件模板失败", e);
                    // 加载失败时使用默认模板
                    return defaultTemplate();
                }
            }
            return current;
        });

        // 替换模板中的变量占位符
        return template
                .replace("${code}", code)
                .replace("${expireMinutes}", String.valueOf(AuthConstants.EMAIL_CODE_EXPIRE_MINUTES))
                .replace("${action}", type.getAction());
    }

    /**
     * 从classpath加载邮件模板文件
     * 如果模板文件不存在，则返回默认模板
     *
     * @return 模板文件内容
     * @throws IOException 当模板文件读取失败时抛出
     */
    private String loadTemplate() throws IOException {
        ClassPathResource resource = new ClassPathResource(UNIVERSAL_TEMPLATE_PATH);
        // 检查模板文件是否存在
        if (!resource.exists()) {
            log.warn("邮件模板文件不存在，使用默认模板: {}", UNIVERSAL_TEMPLATE_PATH);
            return defaultTemplate();
        }
        // 读取模板文件内容，使用UTF-8编码
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

    /**
     * 提供默认的邮件模板
     * 当模板文件不存在或加载失败时使用
     *
     * @return 默认模板内容
     */
    private String defaultTemplate() {
        return "<p>您正在进行<strong>${action}</strong>操作，验证码为：<strong>${code}</strong>，" +
                "有效期 ${expireMinutes} 分钟，请尽快使用。</p>";
    }
}