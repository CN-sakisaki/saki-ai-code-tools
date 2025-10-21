package com.saki.sakiaicodetoolsbackend.model.dto.admin.user;

import com.saki.sakiaicodetoolsbackend.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理员分页获取用户请求对象
 * @author saki酱
 * @version 1.0
 * @since 2025-10-19
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "管理员分页获取用户请求对象")
public class UserQueryRequest extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 2730586292146285187L;

    /**
     * 用户账号
     */
    @Schema(description = "用户账号")
    private String userAccount;

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
     * 用户角色（user/admin）
     */
    @Schema(description = "用户角色（user/admin）")
    private String userRole;

    /**
     * 用户状态（0-禁用，1-正常）
     */
    @Schema(description = "用户状态（0-禁用，1-正常）")
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
     * 最近一次登录的时间
     */
    @Schema(description = "最近一次登录的时间")
    private LocalDateTime lastLoginTime;

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

}
