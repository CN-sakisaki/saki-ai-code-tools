package com.saki.sakiaicodetoolsbackend.service.login.strategy;

import cn.hutool.core.util.StrUtil;
import com.saki.sakiaicodetoolsbackend.exception.BusinessException;
import com.saki.sakiaicodetoolsbackend.exception.ErrorCode;
import com.saki.sakiaicodetoolsbackend.exception.ThrowUtils;
import com.saki.sakiaicodetoolsbackend.mapper.UserMapper;
import com.saki.sakiaicodetoolsbackend.model.dto.LoginRequest;
import com.saki.sakiaicodetoolsbackend.model.entity.User;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 验证码登录策略抽象基类。
 * <p>
 * 该类提供了基于验证码登录的通用实现，包括验证码的校验、Redis缓存操作等。
 * 具体的验证码登录方式（如邮箱验证码、手机验证码）需要继承此类并实现抽象方法。
 * </p>
 *
 * @author saki酱
 * @version 1.0
 * @since 2025-10-17 12:03
 */
public abstract class AbstractCodeLoginStrategy extends AbstractBaseLoginStrategy {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 构造函数。
     *
     * @param userMapper 用户数据访问器
     * @param stringRedisTemplate Redis操作模板
     */
    protected AbstractCodeLoginStrategy(UserMapper userMapper, StringRedisTemplate stringRedisTemplate) {
        super(userMapper);
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 执行验证码登录认证逻辑。
     * <p>
     * 该方法执行验证码登录的核心逻辑，包括：校验用户标识是否为空、验证码是否为空、
     * 从Redis获取缓存的验证码、验证码比对、用户查找等。
     * 认证通过后返回 {@link User} 对象并删除已使用的验证码，否则抛出相应的异常。
     * </p>
     *
     * @param request 登录请求，包含用户的登录凭证和验证码
     * @return 认证通过后的 {@link User} 对象
     * @throws BusinessException 如果认证失败，抛出业务异常
     */
    @Override
    public User authenticate(LoginRequest request) {
        String identifier = getIdentifier(request);
        validateNotBlank(identifier, missingIdentifierMessage());

        String code = request.getLoginCode();
        validateNotBlank(code, "验证码不能为空");

        String redisKey = buildRedisKey(identifier);
        String cachedCode = stringRedisTemplate.opsForValue().get(redisKey);

        ThrowUtils.throwIf(StrUtil.isBlank(cachedCode), ErrorCode.LOGIN_EXPIRED, "验证码已过期");
        ThrowUtils.throwIf(!StrUtil.equals(cachedCode, code), ErrorCode.PARAMS_ERROR, "验证码不正确");

        User user = selectUserByColumn(getIdentifierColumn(), identifier);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, userNotFoundMessage());

        stringRedisTemplate.delete(redisKey);
        return user;
    }

    /**
     * 获取登录凭证标识（如邮箱、手机号等）。
     *
     * @param request 登录请求，包含用户凭证信息
     * @return 登录凭证标识
     */
    protected abstract String getIdentifier(LoginRequest request);

    /**
     * 获取用户查询的列名（如邮箱列、手机号列等）。
     *
     * @return 查询列名
     */
    protected abstract String getIdentifierColumn();

    /**
     * 获取存储验证码的 Redis key（邮箱、手机号等）。
     *
     * @param identifier 用户凭证（邮箱、手机号等）
     * @return Redis key
     */
    protected abstract String buildRedisKey(String identifier);

    /**
     * 获取缺少用户标识时的错误提示信息。
     *
     * @return 错误提示信息
     */
    protected abstract String missingIdentifierMessage();

    /**
     * 获取用户未找到时的错误提示信息。
     *
     * @return 错误提示信息
     */
    protected abstract String userNotFoundMessage();
}
