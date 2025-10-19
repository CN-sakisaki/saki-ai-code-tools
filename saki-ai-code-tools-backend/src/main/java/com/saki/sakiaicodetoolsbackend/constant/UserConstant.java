package com.saki.sakiaicodetoolsbackend.constant;

import com.saki.sakiaicodetoolsbackend.model.enums.UserRoleEnum;

/**
 * 用户通用常量。
 * <p>
 * 该常量类用于暴露在注解等场景中需要引用的基础常量，避免在代码中出现魔法值。
 * </p>
 *
 * @author saki酱
 * @version 1.0
 * @since 2025-10-19
 */
public final class UserConstant {

    /**
     * 管理员角色标识。
     */
    public static final String ADMIN_ROLE = UserRoleEnum.ADMIN.getValue();

    private UserConstant() {
    }
}
