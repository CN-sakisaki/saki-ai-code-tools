package com.saki.sakiaicodetoolsbackend.service.login.strategy;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.saki.sakiaicodetoolsbackend.constant.AuthConstants;
import com.saki.sakiaicodetoolsbackend.exception.ErrorCode;
import com.saki.sakiaicodetoolsbackend.exception.ThrowUtils;
import com.saki.sakiaicodetoolsbackend.mapper.UserMapper;
import com.saki.sakiaicodetoolsbackend.model.dto.LoginRequest;
import com.saki.sakiaicodetoolsbackend.model.entity.User;
import com.saki.sakiaicodetoolsbackend.model.enums.LoginTypeEnum;
import com.saki.sakiaicodetoolsbackend.service.login.LoginStrategy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 邮箱验证码登录策略。
 */
@Component
public class EmailCodeLoginStrategy implements LoginStrategy {

    private static final String USER_TABLE = "saki_ai_code_tools.user";

    private final UserMapper userMapper;
    private final StringRedisTemplate stringRedisTemplate;

    public EmailCodeLoginStrategy(UserMapper userMapper, StringRedisTemplate stringRedisTemplate) {
        this.userMapper = userMapper;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public LoginTypeEnum getLoginType() {
        return LoginTypeEnum.EMAIL_CODE;
    }

    @Override
    public User authenticate(LoginRequest request) {
        String email = request.getUserEmail();
        ThrowUtils.throwIf(StrUtil.isBlank(email), ErrorCode.PARAMS_MISSING, "邮箱不能为空");
        String code = request.getLoginCode();
        ThrowUtils.throwIf(StrUtil.isBlank(code), ErrorCode.PARAMS_MISSING, "验证码不能为空");
        String redisKey = AuthConstants.buildEmailCodeKey(email);
        String cachedCode = stringRedisTemplate.opsForValue().get(redisKey);
        ThrowUtils.throwIf(StrUtil.isBlank(cachedCode), ErrorCode.LOGIN_EXPIRED, "验证码已过期");
        ThrowUtils.throwIf(!StrUtil.equals(cachedCode, code), ErrorCode.PARAMS_ERROR, "验证码不正确");
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(USER_TABLE)
                .where("user_email = ?", email)
                .limit(1);
        User user = userMapper.selectOneByQuery(queryWrapper);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "邮箱未注册");
        stringRedisTemplate.delete(redisKey);
        return user;
    }
}

