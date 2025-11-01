package com.saki.sakiaicodetoolsbackend.service.email.strategy.channel;


import com.saki.sakiaicodetoolsbackend.exception.ErrorCode;
import com.saki.sakiaicodetoolsbackend.exception.ThrowUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 验证渠道工厂类
 *
 * @author saki酱
 * @version 1.0
 * @since 2025-10-28
 */
@Component
public class VerificationChannelFactory {

    /**
     * 验证渠道策略映射表
     */
    private final Map<String, VerificationChannelStrategy> strategyMap;

    /**
     * 构造函数
     *
     * @param strategyMap 验证渠道策略映射表
     */
    public VerificationChannelFactory(Map<String, VerificationChannelStrategy> strategyMap) {
        this.strategyMap = strategyMap;
    }

    /**
     * 根据渠道获取对应的验证策略
     *
     * @param channel 渠道标识
     * @return 对应的验证渠道策略
     * @throws IllegalArgumentException 当渠道不存在时抛出
     */
    public VerificationChannelStrategy getStrategy(String channel) {
        VerificationChannelStrategy strategy = strategyMap.get(channel.toUpperCase());
        ThrowUtils.throwIf(strategy == null, ErrorCode.PARAMS_ERROR, "未知发送渠道: " + channel);
        return strategy;
    }
}
