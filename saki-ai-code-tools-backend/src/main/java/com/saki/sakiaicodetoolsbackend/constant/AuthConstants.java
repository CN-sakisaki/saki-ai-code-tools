package com.saki.sakiaicodetoolsbackend.constant;

/**
 * 认证相关常量。
 */
public final class AuthConstants {

    private AuthConstants() {
    }

    public static final String REFRESH_TOKEN_KEY_PREFIX = "login:refresh:";

    public static final String EMAIL_LOGIN_CODE_PREFIX = "login:code:email:";

    public static final long EMAIL_CODE_EXPIRE_MINUTES = 5L;

    public static String buildRefreshTokenKey(Long userId) {
        return REFRESH_TOKEN_KEY_PREFIX + userId;
    }

    public static String buildEmailCodeKey(String email) {
        return EMAIL_LOGIN_CODE_PREFIX + email.toLowerCase();
    }
}

