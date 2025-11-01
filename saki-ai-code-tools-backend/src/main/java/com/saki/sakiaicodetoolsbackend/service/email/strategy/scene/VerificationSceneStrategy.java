package com.saki.sakiaicodetoolsbackend.service.email.strategy.scene;

import com.saki.sakiaicodetoolsbackend.model.dto.common.SendCodeRequest;

/**
 * 验证码场景策略接口（登录 / 注册 / 修改邮箱 / 重置密码）
 * @author saki酱
 * @version 1.0
 * @since 2025-10-23
 */
public interface VerificationSceneStrategy {
    void execute(SendCodeRequest request);
}
