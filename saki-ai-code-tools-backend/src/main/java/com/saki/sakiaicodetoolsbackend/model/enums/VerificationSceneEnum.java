package com.saki.sakiaicodetoolsbackend.model.enums;

import com.saki.sakiaicodetoolsbackend.constant.AuthConstants;
import lombok.Getter;

/**
 * 邮箱验证码业务场景枚举
 * 用于区分不同的验证码业务前缀
 *
 * @author saki酱
 * @version 1.0
 * @since 2025-10-30
 */
@Getter
public enum VerificationSceneEnum {
    LOGIN(AuthConstants.EMAIL_LOGIN_CODE_PREFIX),
    REGISTER(AuthConstants.EMAIL_REGISTER_CODE_PREFIX),
    UPDATE_EMAIL(AuthConstants.EMAIL_UPDATE_EMAIL_CODE_PREFIX),
    RESET_PASSWORD(AuthConstants.RESET_PASSWORD);

    private final String prefix;

    VerificationSceneEnum(String prefix) {
        this.prefix = prefix;
    }
}
