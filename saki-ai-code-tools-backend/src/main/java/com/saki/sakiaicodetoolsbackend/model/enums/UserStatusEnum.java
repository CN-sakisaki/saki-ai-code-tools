package com.saki.sakiaicodetoolsbackend.model.enums;

import lombok.Getter;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户状态枚举
 * 定义了用户在系统中的所有可能状态，避免在代码中直接使用魔法数值
 * 使用缓存机制提升枚举值查找性能，支持安全的枚举值转换
 *
 * <p>
 * 枚举了用户在系统中的可用状态，以避免直接使用魔法值。当前支持禁用和正常两种状态。
 * 使用缓存机制提升从数值到枚举的转换性能，确保线程安全。
 * </p>
 *
 * <p><b>使用示例：</b></p>
 * <pre>{@code
 * // 根据数值获取枚举
 * UserStatusEnum status = UserStatusEnum.fromValue(1).orElse(UserStatusEnum.ACTIVE);
 *
 * // 获取枚举的显示文本
 * String text = status.getText(); // 返回"正常"
 *
 * // 获取枚举的数值
 * Integer value = status.getValue(); // 返回1
 * }</pre>
 *
 * @author saki酱
 * @version 1.0
 * @since 2025-10-28
 */
@Getter
public enum UserStatusEnum {

    /**
     * 禁用状态
     * 用户处于禁用状态，无法登录和使用系统功能
     * 对应数据库中的数值：0
     */
    DISABLED("禁用", 0),

    /**
     * 正常状态
     * 用户处于正常可用状态，可以正常登录和使用系统
     * 对应数据库中的数值：1
     */
    ACTIVE("正常", 1);

    /**
     * 枚举值缓存映射表
     * 使用ConcurrentHashMap确保线程安全，避免重复构建
     * key: 枚举数值 value: 对应的枚举实例
     */
    private static final Map<Integer, UserStatusEnum> VALUE_CACHE = new ConcurrentHashMap<>();

    // 静态初始化块 - 在类加载时构建缓存
    static {
        for (UserStatusEnum statusEnum : UserStatusEnum.values()) {
            VALUE_CACHE.put(statusEnum.getValue(), statusEnum);
        }
    }

    private final String text;
    private final Integer value;

    UserStatusEnum(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据枚举数值查找对应的用户状态枚举
     * 使用缓存机制提升查找性能，支持安全的空值处理
     *
     * <p><b>使用场景：</b></p>
     * <ul>
     *   <li>从数据库读取数值转换为枚举</li>
     *   <li>从接口参数解析状态枚举</li>
     *   <li>数值到枚举的安全转换</li>
     * </ul>
     *
     * @param value 状态数值（0：禁用，1：正常）
     * @return 对应的枚举包装在Optional中，如果数值不存在则返回空的Optional
     *
     * <p><b>示例：</b></p>
     * <pre>{@code
     * // 安全转换，避免空指针异常
     * UserStatusEnum status = UserStatusEnum.fromValue(1)
     *     .orElseThrow(() -> new IllegalArgumentException("无效的状态值"));
     *
     * // 或者使用默认值
     * UserStatusEnum status = UserStatusEnum.fromValue(99).orElse(UserStatusEnum.ACTIVE);
     * }</pre>
     */
    public static Optional<UserStatusEnum> fromValue(Integer value) {
        return Optional.ofNullable(VALUE_CACHE.get(value));
    }
}

