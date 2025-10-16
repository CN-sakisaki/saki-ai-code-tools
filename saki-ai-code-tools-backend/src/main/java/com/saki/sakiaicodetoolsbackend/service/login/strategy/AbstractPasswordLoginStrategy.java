package com.saki.sakiaicodetoolsbackend.service.login.strategy;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.saki.sakiaicodetoolsbackend.exception.ErrorCode;
import com.saki.sakiaicodetoolsbackend.exception.ThrowUtils;
import com.saki.sakiaicodetoolsbackend.mapper.UserMapper;
import com.saki.sakiaicodetoolsbackend.model.dto.LoginRequest;
import com.saki.sakiaicodetoolsbackend.model.entity.User;
import com.saki.sakiaicodetoolsbackend.service.login.LoginStrategy;

/**
 * 密码登录策略抽象基类。
 */
public abstract class AbstractPasswordLoginStrategy implements LoginStrategy {

    private static final String USER_TABLE = "saki_ai_code_tools.user";

    private final UserMapper userMapper;

    protected AbstractPasswordLoginStrategy(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public User authenticate(LoginRequest request) {
        String identifier = getIdentifier(request);
        ThrowUtils.throwIf(StrUtil.isBlank(identifier), ErrorCode.PARAMS_MISSING, missingIdentifierMessage());
        String password = request.getUserPassword();
        ThrowUtils.throwIf(StrUtil.isBlank(password), ErrorCode.PARAMS_MISSING, "密码不能为空");
        User user = selectUserByColumn(getIdentifierColumn(), identifier);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, userNotFoundMessage());
        String salt = StrUtil.nullToDefault(user.getUserSalt(), "");
        String encrypted = DigestUtil.sha256Hex(password + salt);
        ThrowUtils.throwIf(!StrUtil.equals(encrypted, user.getUserPassword()), ErrorCode.TOKEN_INVALID, "账号或密码错误");
        return user;
    }

    protected abstract String getIdentifier(LoginRequest request);

    protected abstract String getIdentifierColumn();

    protected abstract String missingIdentifierMessage();

    protected abstract String userNotFoundMessage();

    private User selectUserByColumn(String column, String value) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(USER_TABLE)
                .where(column + " = ?", value)
                .limit(1);
        return userMapper.selectOneByQuery(queryWrapper);
    }
}

