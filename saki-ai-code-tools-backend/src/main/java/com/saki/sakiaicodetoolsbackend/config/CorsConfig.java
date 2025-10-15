package com.saki.sakiaicodetoolsbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域资源共享（CORS）配置类。
 * 用于配置Spring MVC的跨域请求策略，允许前端应用从不同源的域名访问后端API。
 *
 * <p><b>技术说明：</b>
 * 使用{@code allowedOriginPatterns("*")}而不是{@code allowedOrigins("*")}，
 * 因为当{@code allowCredentials(true)}时，不能使用具体的{@code "*"}通配符，
 * 否则浏览器会拒绝请求。这是Spring Framework 5.3+的推荐做法。
 *
 * <p><b>安全考虑：</b>
 * 在生产环境中，建议将{@code allowedOriginPatterns}设置为具体的前端域名，
 * 而不是使用通配符{@code "*"}，以提高安全性。
 *
 * @author sakisaki
 * @version 1.0
 * @since 2025-10-15 14:11
 * @see WebMvcConfigurer
 * @see CorsRegistry
 * @see CorsConfiguration
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * 配置跨域请求映射和策略。
     *
     * <p>该方法配置了全局的CORS策略，包括：
     * <ul>
     *   <li>应用路径：所有API路径（/**）</li>
     *   <li>凭证支持：启用，允许携带Cookie</li>
     *   <li>允许的源：使用模式匹配允许所有域名</li>
     *   <li>允许的HTTP方法：GET, POST, PUT, DELETE, OPTIONS</li>
     *   <li>允许的请求头：所有头信息</li>
     *   <li>暴露的响应头：所有头信息</li>
     * </ul>
     *
     * <p><b>注意事项：</b>
     * 在开发环境中使用通配符{@code "*"}是方便的，但在生产环境中应该
     * 替换为具体的前端域名以提高安全性。
     *
     * @param registry CORS注册表，用于配置跨域规则
     * @see CorsRegistry
     * @see CorsRegistration
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 覆盖所有请求
        registry.addMapping("/**")
                // 允许发送 Cookie
                .allowCredentials(true)
                // 放行哪些域名（必须用 patterns，否则 * 会和 allowCredentials 冲突）
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("*");
    }
}

