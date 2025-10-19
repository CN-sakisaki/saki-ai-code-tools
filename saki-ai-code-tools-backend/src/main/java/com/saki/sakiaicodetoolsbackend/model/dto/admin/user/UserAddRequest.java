package com.saki.sakiaicodetoolsbackend.model.dto.admin.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 管理员添加用户请求对象
 * @author saki酱
 * @version 1.0
 * @since 2025-10-19
 */
@Data
@Schema(description = "管理员添加用户请求对象")
public class UserAddRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -692520123315832662L;

    /**
     * 用户账号
     */
    @Schema(description = "用户账号")
    private String userAccount;

    /**
     * 账号密码
     */
    @Schema(description = "账号密码")
    private String userPassword;

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    private String userName;

    /**
     * 用户邮箱
     */
    @Schema(description = "用户邮箱")
    private String userEmail;

    /**
     * 用户手机号
     */
    @Schema(description = "用户手机号")
    private String userPhone;

    /**
     * 用户头像
     */
    @Schema(description = "用户头像")
    private String userAvatar;

    /**
     * 用户简介
     */
    @Schema(description = "用户简介")
    private String userProfile;

    /**
     * 用户角色（user/admin）
     */
    @Schema(description = "用户角色（user/admin）")
    private String userRole;

    /**
     * 用户状态（0-禁用，1-状态）
     */
    @Schema(description = "用户状态（0-禁用，1-状态）")
    private Integer userStatus;

    /**
     * 是否为会员（0-普通会员，1-vip 会员）
     */
    @Schema(description = "是否为会员（0-普通会员，1-vip 会员）")
    private Integer isVip;

}