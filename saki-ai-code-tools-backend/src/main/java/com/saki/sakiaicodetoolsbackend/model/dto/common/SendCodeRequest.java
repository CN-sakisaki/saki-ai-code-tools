package com.saki.sakiaicodetoolsbackend.model.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 通用发送验证码请求对象
 * @author saki酱
 * @version 1.0
 * @since 2025-10-23
 */
@Data
public class SendCodeRequest {

    /** 接收者（邮箱或手机号） */
    @Schema(description = "邮箱或手机号")
    private String receiver;

    /** 渠道类型（EMAIL / PHONE） */
    @Schema(description = "渠道类型")
    private String channel;

    /** 业务场景（LOGIN / REGISTER / RESET_PASSWORD / UPDATE_EMAIL） */
    @Schema(description = "业务场景")
    private String scene;
}
