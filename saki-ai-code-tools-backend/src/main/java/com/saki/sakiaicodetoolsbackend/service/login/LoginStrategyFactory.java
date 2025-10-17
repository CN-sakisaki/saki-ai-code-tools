package com.saki.sakiaicodetoolsbackend.service.login;

import com.saki.sakiaicodetoolsbackend.exception.BusinessException;
import com.saki.sakiaicodetoolsbackend.exception.ErrorCode;
import com.saki.sakiaicodetoolsbackend.exception.ThrowUtils;
import com.saki.sakiaicodetoolsbackend.model.enums.LoginTypeEnum;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 登录策略工厂类。
 * <p>
 * 该类负责管理和提供各种登录策略的实现，采用工厂模式根据登录类型返回对应的策略实例。
 * 工厂在初始化时会自动收集所有实现了 {@link LoginStrategy} 接口的Bean，并建立登录类型与策略的映射关系。
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * {@code
 * LoginStrategy strategy = loginStrategyFactory.getStrategy(LoginTypeEnum.ACCOUNT_PASSWORD);
 * User user = strategy.authenticate(loginRequest);
 * }
 * </pre>
 * </p>
 * @author saki酱
 * @version 1.0
 * @since 2025-10-17 12:02
 */
@Component
public class LoginStrategyFactory {
    /**
     * 登录策略映射表，存储登录类型与对应策略实例的映射关系。
     */
    private final Map<LoginTypeEnum, LoginStrategy> strategyMap = new EnumMap<>(LoginTypeEnum.class);

    /**
     * 构造函数，初始化登录策略映射。
     * <p>
     * 通过Spring的依赖注入自动收集所有 {@link LoginStrategy} 的实现Bean，
     * 并根据每个策略支持的登录类型建立映射关系。
     * </p>
     *
     * @param strategies 所有登录策略实现的列表，由Spring自动注入
     * @throws IllegalArgumentException 如果存在重复的登录类型映射
     */
    public LoginStrategyFactory(List<LoginStrategy> strategies) {
        for (LoginStrategy strategy : strategies) {
            strategyMap.put(strategy.getLoginType(), strategy);
        }
    }

    /**
     * 根据登录类型获取对应的登录策略实例。
     * <p>
     * 如果请求的登录类型不存在对应的策略，将抛出业务异常。
     * </p>
     *
     * @param loginTypeEnum 登录类型枚举
     * @return 对应的登录策略实例
     * @throws BusinessException 如果登录类型不支持，抛出参数错误异常
     */
    public LoginStrategy getStrategy(LoginTypeEnum loginTypeEnum) {
        LoginStrategy strategy = strategyMap.get(loginTypeEnum);
        ThrowUtils.throwIf(strategy == null,ErrorCode.PARAMS_ERROR, "不支持的登录方式");
        return strategy;
    }
}

