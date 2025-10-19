package com.saki.sakiaicodetoolsbackend.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户更新个人信息请求对象
 * @author saki酱
 * @version 1.0
 * @since 2025-10-19
 */
@Data
@Schema(description = "用户更新个人信息请求对象")
public class UserProfileUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -4715457621622352627L;

    /**
     * 主键
     */
    @Schema(description = "主键")
    private Long id;

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    private String userName;

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
     * 最近一次编辑时间
     */
    @Schema(description = "最近一次编辑时间")
    private LocalDateTime editTime;

}
