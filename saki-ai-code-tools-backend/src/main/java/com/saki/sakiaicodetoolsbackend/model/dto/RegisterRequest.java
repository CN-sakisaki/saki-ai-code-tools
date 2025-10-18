package com.saki.sakiaicodetoolsbackend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 注册请求参数。
 *
 * <p>
 * 用于接收用户注册时提交的核心信息，包含账号、密码、确认密码以及可选的邀请码。
 * 账号、密码和确认密码均为必填项，邀请码可根据业务场景自行决定是否提供。
 * </p>
 * @author saki酱
 * @version 1.0
 * @since 2025-10-18
 */
@Data
@Schema(description = "用户注册请求参数")
public class RegisterRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 6184471532299272944L;

    /**
     * 用户账号，系统登录的唯一标识。
     */
    @Schema(description = "用户账号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userAccount;

    /**
     * 用户密码，需与确认密码一致。
     */
    @Schema(description = "用户密码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userPassword;

    /**
     * 确认密码，用于校验两次输入是否一致。
     */
    @Schema(description = "确认密码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String confirmPassword;

    /**
     * 邀请码，可选字段。
     */
    @Schema(description = "邀请码", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String inviteCode;
}

