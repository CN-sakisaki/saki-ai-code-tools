package com.saki.sakiaicodetoolsbackend.model.enums;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * 登录类型枚举。
 * @author saki酱
 * @version 1.0
 * @since 2025-10-17 11:38
 */
@Getter
public enum LoginTypeEnum {

    ACCOUNT_PASSWORD("ACCOUNT_PASSWORD"),
    PHONE_PASSWORD("PHONE_PASSWORD"),
    EMAIL_CODE("EMAIL_CODE"),
    PHONE_CODE("PHONE_CODE");

    private final String value;

    LoginTypeEnum(String value) {
        this.value = value;
    }

    /**
     * 根据字符串值解析登录类型，忽略大小写。
     * <p>使用 Optional 避免 NullPointerException</p>
     *
     * @param value 字符串值
     * @return 登录类型
     */
    public static Optional<LoginTypeEnum> fromValue(String value) {
        if (StrUtil.isBlank(value)) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                // 过滤：忽略大小写比较枚举值的value字段与输入值
                .filter(item -> StrUtil.equalsIgnoreCase(item.value, value))
                // 返回第一个匹配的枚举值，包装在Optional中
                .findFirst();
    }
}

