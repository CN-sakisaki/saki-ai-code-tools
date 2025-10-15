package com.saki.sakiaicodetoolsbackend.model.enums;

import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户角色枚举
 * <p>
 * 定义了系统中所有可用的用户角色类型，用于权限控制和角色管理
 * </p>
 *
 * @author saki酱
 * @version 1.0
 * @since 2025-10-15 20:43
 */
@Getter
public enum UserRoleEnum {
    USER("用户", "user"),
    ADMIN("管理员", "admin");

    private final String text;
    private final String value;

    /**
     * 角色值缓存映射
     * <p>
     * 使用线程安全的ConcurrentHashMap缓存角色值与枚举实例的映射关系，
     * 提高通过value查找枚举实例的性能
     * </p>
     */
    private static final Map<String, UserRoleEnum> VALUE_MAP = new ConcurrentHashMap<>();

    // 静态初始化块：在类加载时构建缓存映射
    static {
        for (UserRoleEnum role : UserRoleEnum.values()) {
            VALUE_MAP.put(role.getValue(), role);
        }
    }

    /**
     * 枚举构造函数
     *
     * @param text  角色显示文本，用于界面展示
     * @param value 角色值，用于程序内部识别
     */
    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 通过角色值获取对应的枚举实例
     * <p>
     * 使用缓存映射进行快速查找，时间复杂度为O(1)
     * </p>
     *
     * @param value 角色值，如："user"、"admin"
     * @return 对应的UserRoleEnum枚举实例，如果未找到则返回null
     * @see #USER
     * @see #ADMIN
     */
    public static UserRoleEnum getByValue(String value) {
        return VALUE_MAP.get(value);
    }
}
