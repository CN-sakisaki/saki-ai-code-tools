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
     * 私有构造方法，防止类被实例化。
     */
    private UserConstants() {
    }

    // ===================== 用户默认信息常量 =====================

    /**
     * 默认的用户名前缀。
     * 用于在用户注册时未指定用户名时自动生成用户名。
     */
    public static final String DEFAULT_USERNAME_PREFIX = "ai_";

    /**
     * 默认的用户简介。
     * 当用户未设置个人简介时显示此默认内容。
     */
    public static final String DEFAULT_PROFILE = "这个人好懒，什么都没留下...";

    /**
     * 默认的用户头像路径。
     * 当用户未上传自定义头像时使用的默认头像路径。
     */
    public static final String DEFAULT_AVATAR_PATH = "/static/images/default-avatar.png";

    // ===================== 用户状态默认值常量 =====================

    /**
     * 默认的用户角色值。
     * 新用户注册时默认分配的角色。
     */
    public static final String DEFAULT_USER_ROLE = UserRoleEnum.USER.getValue();

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

    // ===================== 邀请码相关常量 =====================

    /**
     * 邀请码的字符池。
     * 包含大写字母和数字，用于生成唯一的邀请码。
     */
    public static final String INVITE_CODE_CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

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

    // ===================== 安全相关常量 =====================

    /**
     * 用户盐值的字节长度。
     * 用于密码加密的盐值的字节数。
     */
    public static final int USER_SALT_BYTE_LENGTH = 8;
}