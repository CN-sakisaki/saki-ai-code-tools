package com.saki.sakiaicodetoolsbackend.service.email.strategy.scene;

import com.saki.sakiaicodetoolsbackend.exception.ErrorCode;
import com.saki.sakiaicodetoolsbackend.exception.ThrowUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 验证场景工厂类
 * 根据场景类型获取对应的验证策略
 *
 * @author saki酱
 * @version 1.0
 * @since 2025-10-28
 */
@Component
public class VerificationSceneFactory {

    /**
     * 验证场景策略映射表
     */
    private final Map<String, VerificationSceneStrategy> strategyMap;

    /**
     * 构造函数
     *
     * @param strategyMap 验证场景策略映射表
     */
    public VerificationSceneFactory(Map<String, VerificationSceneStrategy> strategyMap) {
        this.strategyMap = strategyMap;
    }

    /**
     * 根据场景类型获取对应的验证策略
     *
     * @param scene 场景标识
     * @return 对应的验证场景策略
     * @throws IllegalArgumentException 当场景类型不存在时抛出
     */
    public VerificationSceneStrategy getStrategy(String scene) {
        VerificationSceneStrategy strategy = strategyMap.get(scene.toUpperCase());
        ThrowUtils.throwIf(strategy == null, ErrorCode.PARAMS_ERROR, "未知验证码场景: " + scene);
        return strategy;
    }
}
