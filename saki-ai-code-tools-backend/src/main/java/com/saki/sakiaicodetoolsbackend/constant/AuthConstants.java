package com.saki.sakiaicodetoolsbackend.constant;

/**
 * 认证相关常量类。
 * 该类定义了认证系统中使用的各种常量，包括Redis键前缀、过期时间等。
 * 采用final类和私有构造方法确保不可实例化。
 *
 * @author saki酱
 * @version 1.0
 * @since 2025-10-17 13:37
 */
public final class AuthConstants {

    /**
     * 私有构造方法，防止类被实例化。
     */
    private AuthConstants() {
    }

    // ===================== Redis键前缀常量 =====================

    /**
     * 刷新令牌在Redis中的键前缀。
     * 完整键格式：login:refresh:{userId}
     */
    public static final String REFRESH_TOKEN_KEY_PREFIX = "login:refresh:";

    /**
     * 邮箱登录验证码在Redis中的键前缀。
     * 完整键格式：login:code:email:{email}
     */
    public static final String EMAIL_LOGIN_CODE_PREFIX = "login:code:email:";

    /**
     * 手机登录验证码在Redis中的键前缀。
     * 完整键格式：login:code:phone:{phone}
     */
    public static final String EMAIL_LOGIN_PHONE_PREFIX = "login:code:phone:";

    // ===================== 过期时间常量 =====================

    /**
     * 邮箱验证码过期时间（分钟）。
     * 默认值：5分钟
     */
    public static final long EMAIL_CODE_EXPIRE_MINUTES = 5L;

    /**
     * 手机验证码过期时间（分钟）。
     * 默认值：5分钟
     */
    public static final long PHONE_CODE_EXPIRE_MINUTES = 5L;

    /**
     * AccessToken 在 Cookie 中的键名。
     */
    public static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";

    /**
     * 认证相关 Cookie 的默认 Path。
     */
    public static final String AUTH_COOKIE_PATH = "/";

    // ===================== 键构建方法 =====================

    /**
     * 构建刷新令牌的Redis存储键。
     *
     * @param userId 用户ID，不能为null
     * @return 格式化的Redis键，如："login:refresh:123"
     * @throws IllegalArgumentException 如果userId为null
     */
    public static String buildRefreshTokenKey(Long userId) {
        return REFRESH_TOKEN_KEY_PREFIX + userId;
    }

    /**
     * 构建邮箱验证码的Redis存储键。
     * 自动将邮箱转换为小写以确保键的一致性。
     *
     * @param email 邮箱地址，不能为null或空
     * @return 格式化的Redis键，如："login:code:email:user@example.com"
     * @throws IllegalArgumentException 如果email为null或空
     */
    public static String buildEmailCodeKey(String email) {
        return EMAIL_LOGIN_CODE_PREFIX + email.toLowerCase();
    }

    /**
     * 构建手机验证码的Redis存储键。
     * 自动将手机号转换为小写以确保键的一致性。
     *
     * @param phone 手机号码，不能为null或空
     * @return 格式化的Redis键，如："login:code:phone:13800138000"
     * @throws IllegalArgumentException 如果phone为null或空
     */
    public static String buildPhoneCodeKey(String phone) {
        return EMAIL_LOGIN_PHONE_PREFIX + phone.toLowerCase();
    }
}

