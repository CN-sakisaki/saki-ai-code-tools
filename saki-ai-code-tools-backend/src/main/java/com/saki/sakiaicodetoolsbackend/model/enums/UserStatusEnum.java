package com.saki.sakiaicodetoolsbackend.model.enums;

import lombok.Getter;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户状态枚举。
 *
 * <p>
 * 枚举了用户在系统中的可用状态，以避免直接使用魔法值。当前支持禁用和正常两种状态。
 * </p>
 */
@Getter
public enum UserStatusEnum {

    /**
     * 禁用状态（值为0）。
     */
    DISABLED("禁用", 0),

    /**
     * 正常状态（值为1）。
     */
    ACTIVE("正常", 1);

    private final String text;
    private final Integer value;

    private static final Map<Integer, UserStatusEnum> VALUE_CACHE = new ConcurrentHashMap<>();

    static {
        for (UserStatusEnum statusEnum : UserStatusEnum.values()) {
            VALUE_CACHE.put(statusEnum.getValue(), statusEnum);
        }
    }

    UserStatusEnum(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据枚举值查找对应的用户状态。
     *
     * @param value 状态值
     * @return 对应的枚举，可用于安全的值转换
     */
    public static Optional<UserStatusEnum> fromValue(Integer value) {
        return Optional.ofNullable(VALUE_CACHE.get(value));
    }
}

