package com.saki.sakiaicodetoolsbackend.model.enums;

import cn.hutool.core.util.StrUtil;

import java.util.Arrays;
import java.util.Optional;

/**
 * 登录类型枚举。
 */
public enum LoginTypeEnum {

    ACCOUNT_PASSWORD("ACCOUNT_PASSWORD"),
    PHONE_PASSWORD("PHONE_PASSWORD"),
    EMAIL_CODE("EMAIL_CODE");

    private final String value;

    LoginTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * 根据字符串值解析登录类型，忽略大小写。
     *
     * @param value 字符串值
     * @return 登录类型
     */
    public static Optional<LoginTypeEnum> fromValue(String value) {
        if (StrUtil.isBlank(value)) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(item -> StrUtil.equalsIgnoreCase(item.value, value))
                .findFirst();
    }
}

