package com.saki.sakiaicodetoolsbackend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Spring MVC Jackson JSON 配置类
 *
 * <p>主要用于配置 Jackson 的 ObjectMapper，解决 Long 类型在前端精度丢失问题</p>
 *
 * Spring MVC Json 配置
 * @author saki酱
 * @version 1.0
 * @since 2025-10-21
 */
@JsonComponent
public class JsonConfig {

    /**
     * 创建自定义 Jackson ObjectMapper Bean
     *
     * <p>主要功能：
     * <ul>
     *   <li>禁用 XML 映射器</li>
     *   <li>将 Long 和 long 类型序列化为字符串，避免前端精度丢失</li>
     * </ul>
     * </p>
     *
     * @param builder Jackson2ObjectMapperBuilder Spring 提供的构建器
     * @return ObjectMapper 自定义配置的 ObjectMapper 实例
     */
    @Bean
    public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
        // 使用构建器创建 ObjectMapper，禁用 XML 映射功能
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();

        // 创建简单模块并添加 Long 类型序列化器
        SimpleModule module = new SimpleModule();
        // Long 包装类使用字符串序列化
        module.addSerializer(Long.class, ToStringSerializer.instance);
        // long 基本类型使用字符串序列化
        module.addSerializer(Long.TYPE, ToStringSerializer.instance);
        // 注册自定义模块到 ObjectMapper
        objectMapper.registerModule(module);

        return objectMapper;
    }
}

