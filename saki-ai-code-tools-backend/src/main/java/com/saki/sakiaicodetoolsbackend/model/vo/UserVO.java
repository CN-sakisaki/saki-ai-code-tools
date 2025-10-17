package com.saki.sakiaicodetoolsbackend.model.vo;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.saki.sakiaicodetoolsbackend.model.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户登录成功后返回给前端的视图对象。
 * @author saki酱
 * @version 1.0
 * @since 2025-10-17 11:39
 */
@Data
@Schema(description = "用户信息视图对象")
public class UserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -989032212459964155L;

    private Long id;

    private String userAccount;

    private String userEmail;

    private String userPhone;

    private String userAvatar;

    private String userProfile;

    private String userRole;

    private Integer userStatus;

    private Integer isVip;

    private LocalDateTime vipStartTime;

    private LocalDateTime vipEndTime;

    private String inviteCode;

    private LocalDateTime lastLoginTime;

    private String lastLoginIp;

    private LocalDateTime editTime;

    private LocalDateTime createTime;

    private String accessToken;

    public void copyUserInfoFrom(User user) {
        BeanUtil.copyProperties(user, this, CopyOptions.create().ignoreNullValue());
    }

}

