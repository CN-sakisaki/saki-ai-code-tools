package com.saki.sakiaicodetoolsbackend.service.login.strategy;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.saki.sakiaicodetoolsbackend.exception.BusinessException;
import com.saki.sakiaicodetoolsbackend.exception.ErrorCode;
import com.saki.sakiaicodetoolsbackend.exception.ThrowUtils;
import com.saki.sakiaicodetoolsbackend.mapper.UserMapper;
import com.saki.sakiaicodetoolsbackend.model.dto.login.LoginRequest;
import com.saki.sakiaicodetoolsbackend.model.entity.User;

/**
 * 密码登录策略抽象基类。
 * <p>
 * 该类提供了基于密码登录的通用实现，包括密码的加密校验、用户身份验证等。
 * 具体的密码登录方式（如账号密码、手机号密码）需要继承此类并实现抽象方法。
 * </p>
 *
 * @author saki酱
 * @version 1.0
 * @since 2025-10-17 12:03
 */
public abstract class AbstractPasswordLoginStrategy extends AbstractBaseLoginStrategy {

    /**
     * 构造函数。
     *
     * @param userMapper 用户数据访问器
     */
    protected AbstractPasswordLoginStrategy(UserMapper userMapper) {
        super(userMapper);
    }

    /**
     * 执行密码登录认证逻辑。
     * <p>
     * 该方法执行认证的核心逻辑，包括：校验用户标识是否为空、密码是否为空、通过用户标识查找用户信息、密码加密校验等。
     * 认证通过后返回 {@link User} 对象，否则抛出相应的异常。
     * </p>
     * @param request 登录请求，包含用户的登录凭证（标识、密码等）
     * @return 认证通过后的 {@link User} 对象
     * @throws BusinessException 如果认证失败，抛出 {@link BusinessException} 异常，包含错误码和描述信息
     */
    @Override
    public User authenticate(LoginRequest request) {
        // 获取登录凭证
        String identifier = getIdentifier(request);
        validateNotBlank(identifier, missingIdentifierMessage());
        // 获取用户密码
        String password = request.getUserPassword();
        validateNotBlank(password, "密码不能为空");
        // 根据标识查找用户
        User user = selectUserByColumn(getIdentifierColumn(), identifier);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, userNotFoundMessage());
        // 校验密码
        String salt = StrUtil.nullToDefault(user.getUserSalt(), "");
        String encrypted = DigestUtil.sha256Hex(password + salt);
        ThrowUtils.throwIf(!StrUtil.equals(encrypted, user.getUserPassword()), ErrorCode.TOKEN_INVALID, "账号或密码错误");

        return user;
    }

    /**
     * 获取用户登录凭证标识（如账号、手机号等）。
     * <p>
     * 具体实现类需要实现该方法，返回用户请求中的标识（如账号或手机号等）。
     * </p>
     *
     * @param request 登录请求，包含用户的登录凭证
     * @return 用户标识（如账号、手机号等）
     */
    protected abstract String getIdentifier(LoginRequest request);

    /**
     * 获取用于查询的列名（如“账号列”或“手机号列”等）。
     * <p>
     * 具体实现类需要实现该方法，返回相应的列名，通常与数据库表中的字段一致。
     * </p>
     *
     * @return 数据库中的列名（如 "user_account" 或 "user_email"）
     */
    protected abstract String getIdentifierColumn();

    /**
     * 获取缺少用户标识时的错误提示信息。
     * <p>
     * 具体实现类需要提供适当的错误信息，通常是缺少用户名、手机号等提示。
     * </p>
     *
     * @return 错误提示信息，指示用户标识缺失
     */
    protected abstract String missingIdentifierMessage();

    /**
     * 获取用户未找到时的错误提示信息。
     * <p>
     * 具体实现类需要提供适当的错误信息，通常是用户未注册或用户不存在等提示。
     * </p>
     *
     * @return 错误提示信息，指示用户未找到
     */
    protected abstract String userNotFoundMessage();

}

