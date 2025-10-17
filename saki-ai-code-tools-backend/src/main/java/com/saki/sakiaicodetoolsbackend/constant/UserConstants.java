package com.saki.sakiaicodetoolsbackend.constant;

import com.saki.sakiaicodetoolsbackend.model.enums.UserRoleEnum;
import com.saki.sakiaicodetoolsbackend.model.enums.UserStatusEnum;
import com.saki.sakiaicodetoolsbackend.model.enums.VipStatusEnum;

/**
 * 用户相关的业务常量。
 */
public final class UserConstants {

    private UserConstants() {
    }

    /** 默认的用户名此前缀。 */
    public static final String DEFAULT_USERNAME_PREFIX = "ai_";

    /** 默认的用户简介。 */
    public static final String DEFAULT_PROFILE = "这个人好懒，什么都没留下...";

    /** 默认的用户头像路径。 */
    public static final String DEFAULT_AVATAR_PATH = "/static/images/default-avatar.png";

    /** 默认的用户角色值。 */
    public static final String DEFAULT_USER_ROLE = UserRoleEnum.USER.getValue();

    /** 默认的用户状态值。 */
    public static final Integer DEFAULT_USER_STATUS = UserStatusEnum.ACTIVE.getValue();

    /** 默认的会员状态值。 */
    public static final Integer DEFAULT_VIP_STATUS = VipStatusEnum.NON_VIP.getValue();

    /** 邀请码的字符池。 */
    public static final String INVITE_CODE_CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    /** 邀请码长度。 */
    public static final int INVITE_CODE_LENGTH = 6;

    /** 生成邀请码的最大重试次数。 */
    public static final int INVITE_CODE_MAX_RETRY = 5;

    /** 用户盐值的字节长度。 */
    public static final int USER_SALT_BYTE_LENGTH = 16;
}

