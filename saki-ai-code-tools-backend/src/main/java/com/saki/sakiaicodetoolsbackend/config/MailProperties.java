package com.saki.sakiaicodetoolsbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 读取邮件配置
 * @author saki酱
 * @version 1.0
 * @since 2025-10-18
 */
@Data
@Component
@ConfigurationProperties(prefix = "mail")
public class MailProperties {

    /**
     * 发送邮件源地址
     */
    private String mailFrom;
}
