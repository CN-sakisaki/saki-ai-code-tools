package com.saki.sakiaicodetoolsbackend.model.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import java.io.Serial;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户表 实体类。
 *
 * @author saki酱
 * @since 2025-10-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户表")
@Table(value = "user", schema = "saki_ai_code_tools")
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 5766406069823689912L;

    /**
     * 主键
     */
    @Id(keyType = KeyType.Auto)
    @Schema(description = "主键")
    private Long id;

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

    /**
     * vip 会员开始时间
     */
    @Schema(description = "vip 会员开始时间")
    private LocalDateTime vipStartTime;

    /**
     * vip 会员结束时间
     */
    @Schema(description = "vip 会员结束时间")
    private LocalDateTime vipEndTime;

    /**
     * 邀请码
     */
    @Schema(description = "邀请码")
    private String inviteCode;

    /**
     * 最近一次登录的时间
     */
    @Schema(description = "最近一次登录的时间")
    private LocalDateTime lastLoginTime;

    /**
     * 最近一次登录的 IP 地址
     */
    @Schema(description = "最近一次登录的 IP 地址")
    private String lastLoginIp;

    /**
     * 最近一次编辑时间
     */
    @Schema(description = "最近一次编辑时间")
    private LocalDateTime editTime;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 盐值
     */
    @Schema(description = "盐值")
    private String userSalt;

    /**
     * 逻辑删除（0-未删除，1-已删除）
     */
    @Schema(description = "逻辑删除（0-未删除，1-已删除）")
    private Integer isDelete;

}
