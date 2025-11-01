package com.saki.sakiaicodetoolsbackend.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户更新个人手机号请求对象
 * @author saki酱
 * @version 1.0
 * @since 2025-10-19
 */
@Data
@Schema(description = "用户更新个人手机号请求对象")
public class UserPhoneUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 7525909182819188409L;

    /**
     * 主键
     */
    @Schema(description = "主键")
    private Long id;

    /**
     * 账号密码
     */
    @Schema(description = "账号密码")
    private String userPassword;

    /**
     * 新手机号
     */
    @Schema(description = "新手机号")
    private String newPhone;

    /**
     * 短信验证码
     */
    @Schema(description = "短信验证码")
    private String phoneCode;
}
