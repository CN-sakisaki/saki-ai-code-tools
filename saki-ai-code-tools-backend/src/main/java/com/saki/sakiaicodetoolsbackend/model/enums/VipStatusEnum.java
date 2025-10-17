package com.saki.sakiaicodetoolsbackend.model.enums;

import lombok.Getter;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 会员状态枚举。
 *
 * <p>
 * 统一管理用户是否为会员的状态值，避免散落的常量定义。
 * </p>
 */
@Getter
public enum VipStatusEnum {

    /**
     * 普通会员（值为0）。
     */
    NON_VIP("普通会员", 0),

    /**
     * VIP 会员（值为1）。
     */
    VIP("VIP会员", 1);

    private final String text;
    private final Integer value;

    private static final Map<Integer, VipStatusEnum> VALUE_CACHE = new ConcurrentHashMap<>();

    static {
        for (VipStatusEnum vipStatusEnum : VipStatusEnum.values()) {
            VALUE_CACHE.put(vipStatusEnum.getValue(), vipStatusEnum);
        }
    }

    VipStatusEnum(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据枚举值获取对应的会员状态。
     *
     * @param value 状态值
     * @return 对应的枚举
     */
    public static Optional<VipStatusEnum> fromValue(Integer value) {
        return Optional.ofNullable(VALUE_CACHE.get(value));
    }
}

