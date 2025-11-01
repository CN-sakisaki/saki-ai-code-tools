package com.saki.sakiaicodetoolsbackend.constant;

import com.saki.sakiaicodetoolsbackend.model.enums.UserRoleEnum;
import com.saki.sakiaicodetoolsbackend.model.enums.UserStatusEnum;
import com.saki.sakiaicodetoolsbackend.model.enums.VipStatusEnum;

/**
 * 用户相关的业务常量类。
 * 该类定义了用户模块中使用的各种业务常量，包括默认值、配置参数等。
 * 采用final类和私有构造方法确保不可实例化。
 *
 * @author saki酱
 * @version 1.0
 * @since 2025-10-18
 */
public final class UserConstants {

    /**
     * 默认的用户名前缀。
     * 用于在用户注册时未指定用户名时自动生成用户名。
     */
    public static final String DEFAULT_USERNAME_PREFIX = "ai_";

    // ===================== 用户默认信息常量 =====================
    /**
     * 默认的用户角色值。
     * 新用户注册时默认分配的角色。
     */
    public static final String DEFAULT_USER_ROLE = UserRoleEnum.USER.getValue();

    // ===================== 用户状态默认值常量 =====================
    /**
     * 默认的用户状态值。
     * 新用户注册时的默认状态，表示用户账号已激活。
     */
    public static final Integer DEFAULT_USER_STATUS = UserStatusEnum.ACTIVE.getValue();
    /**
     * 默认的会员状态值。
     * 新用户注册时的默认会员状态，表示非会员用户。
     */
    public static final Integer DEFAULT_VIP_STATUS = VipStatusEnum.NON_VIP.getValue();
    /**
     * 邀请码的字符池。
     * 包含大写字母和数字，用于生成唯一的邀请码。
     */
    public static final String INVITE_CODE_CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    // ===================== 邀请码相关常量 =====================
    /**
     * 邀请码长度。
     * 生成的邀请码的字符数量。
     */
    public static final int INVITE_CODE_LENGTH = 6;
    /**
     * 生成邀请码的最大重试次数。
     * 当生成的邀请码与现有邀请码冲突时，最多重试的次数。
     */
    public static final int INVITE_CODE_MAX_RETRY = 5;
    /**
     * 用户盐值的字节长度。
     * 用于密码加密的盐值的字节数。
     */
    public static final int USER_SALT_BYTE_LENGTH = 8;

    // ===================== 安全相关常量 =====================
    /**
     * Session 中存储登录用户的键名。
     */
    public static final String USER_LOGIN_STATE = "USER_LOGIN_STATE";
    /**
     * 登录 Session 默认过期时间（秒）。
     * 30 天。
     */
    public static final int SESSION_TIMEOUT_SECONDS = 30 * 24 * 60 * 60;

    /**
     * 私有构造方法，防止类被实例化。
     */
    private UserConstants() {
    }
}