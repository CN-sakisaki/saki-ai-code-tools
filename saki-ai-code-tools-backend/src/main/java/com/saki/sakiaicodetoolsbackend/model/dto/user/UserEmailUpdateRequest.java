package com.saki.sakiaicodetoolsbackend.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户更新个人邮箱请求对象
 * @author saki酱
 * @version 1.0
 * @since 2025-10-19
 */
@Data
@Schema(description = "用户更新个人邮箱请求对象")
public class UserEmailUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -4921418829958785423L;

    @Schema(description = "主键")
    private Long id;

    /**
     * 账号密码
     */
    @Schema(description = "账号密码")
    private String userPassword;

    /**
     * 新邮箱地址
     */
    @Schema(description = "新邮箱地址")
    private String newEmail;

    /**
     * 邮箱验证码
     */
    @Schema(description = "邮箱验证码")
    private String emailCode;
}
