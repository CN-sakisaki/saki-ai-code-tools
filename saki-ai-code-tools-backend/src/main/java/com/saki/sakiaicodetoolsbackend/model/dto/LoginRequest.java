package com.saki.sakiaicodetoolsbackend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录请求参数。
 *
 * @author sak
 */
@Data
@Schema(description = "登录请求参数")
public class LoginRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 登录类型：账号密码、手机号密码、邮箱验证码等。
     */
    @Schema(description = "登录类型：ACCOUNT_PASSWORD、PHONE_PASSWORD、EMAIL_CODE")
    private String loginType;

    /**
     * 用户账号。
     */
    @Schema(description = "用户账号")
    private String userAccount;

    /**
     * 用户密码。
     */
    @Schema(description = "用户密码")
    private String userPassword;

    /**
     * 用户手机号。
     */
    @Schema(description = "用户手机号")
    private String userPhone;

    /**
     * 用户邮箱。
     */
    @Schema(description = "用户邮箱")
    private String userEmail;

    /**
     * 登录验证码。
     */
    @Schema(description = "登录验证码")
    private String loginCode;
}

