package com.saki.sakiaicodetoolsbackend.service.login;

import com.saki.sakiaicodetoolsbackend.exception.BusinessException;
import com.saki.sakiaicodetoolsbackend.exception.ErrorCode;
import com.saki.sakiaicodetoolsbackend.model.enums.LoginTypeEnum;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 登录策略工厂。
 */
@Component
public class LoginStrategyFactory {

    private final Map<LoginTypeEnum, LoginStrategy> strategyMap = new EnumMap<>(LoginTypeEnum.class);

    public LoginStrategyFactory(List<LoginStrategy> strategies) {
        for (LoginStrategy strategy : strategies) {
            strategyMap.put(strategy.getLoginType(), strategy);
        }
    }

    public LoginStrategy getStrategy(LoginTypeEnum loginTypeEnum) {
        LoginStrategy strategy = strategyMap.get(loginTypeEnum);
        if (strategy == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的登录方式");
        }
        return strategy;
    }
}

