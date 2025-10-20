package com.saki.sakiaicodetoolsbackend.service.login.strategy;

import com.mybatisflex.core.query.QueryWrapper;
import com.saki.sakiaicodetoolsbackend.exception.BusinessException;
import com.saki.sakiaicodetoolsbackend.exception.ErrorCode;
import com.saki.sakiaicodetoolsbackend.exception.ThrowUtils;
import com.saki.sakiaicodetoolsbackend.mapper.UserMapper;
import com.saki.sakiaicodetoolsbackend.model.entity.User;
import com.saki.sakiaicodetoolsbackend.service.login.LoginStrategy;
import org.apache.commons.lang3.StringUtils;

/**
 * 登录策略的基础抽象类，提供各类登录策略通用的基础方法。
 * <p>
 * 该类实现了 {@link LoginStrategy} 接口，但并未实现其中的所有抽象方法。
 * 主要用于封装多个登录策略之间共享的通用逻辑，例如用户查询、参数校验等。
 * 具体的登录策略（如账号密码登录、邮箱验证码登录、手机号验证码登录等）
 * 可继承该类并实现其自身的登录逻辑。
 * </p>
 *
 * <h3>设计说明：</h3>
 * <ul>
 *   <li>该类不能被直接实例化。</li>
 *   <li>子类通常为不同类型的登录策略抽象类，如 {@code AbstractPasswordLoginStrategy}、
 *   {@code AbstractCodeLoginStrategy}。</li>
 *   <li>通过模板方法模式（Template Method Pattern）和策略模式（Strategy Pattern）
 *   的结合，实现可扩展的登录方式。</li>
 * </ul>
 * @author saki酱
 * @version 1.0
 * @since 2025-10-17 14:26
 */
public abstract class AbstractBaseLoginStrategy implements LoginStrategy {
    /**
     * 用户表名称（完整限定名）。
     * <p>用于构建 {@link QueryWrapper} 查询语句。</p>
     */
    private static final String USER_TABLE = "saki_ai_code_tools.user";

    /**
     * 用户数据访问对象，用于执行数据库操作。
     */
    protected final UserMapper userMapper;

    /**
     * 构造方法，注入 {@link UserMapper} 依赖。
     *
     * @param userMapper 用户数据访问层对象，用于数据库交互
     */
    protected AbstractBaseLoginStrategy(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 通用的用户查询逻辑。
     * <p>
     * 根据指定的列名和列值查询用户信息。该方法适用于所有基于数据库字段的登录方式，
     * 例如通过邮箱、手机号或账号查询用户。
     * </p>
     *
     * @param column 查询列名（例如 {@code "user_email"}、{@code "user_phone"}）
     * @param value  查询值（用户输入的邮箱、手机号或账号）
     * @return 查询到的 {@link User} 对象，如果未找到则返回 {@code null}
     */
    protected User selectUserByColumn(String column, String value) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(USER_TABLE)
                // 查询未逻辑删除的用户
                .where(column + " = ?", value)
                // 再匹配具体字段
                .where(User::getIsDelete).eq(0)
                .limit(1);
        return userMapper.selectOneByQuery(queryWrapper);
    }

    /**
     * 校验字段是否为空的通用方法。
     * <p>
     * 用于在登录过程中验证请求参数的合法性，如果字段为空则抛出业务异常。
     * </p>
     *
     * @param field   需要校验的字段值
     * @param message 当字段为空时抛出的错误提示信息
     * @throws BusinessException 当字段为空时，抛出携带 {@link ErrorCode#PARAMS_MISSING} 的业务异常
     */
    protected void validateNotBlank(String field, String message) {
        ThrowUtils.throwIf(StringUtils.isBlank(field), ErrorCode.PARAMS_MISSING, message);
    }
}
