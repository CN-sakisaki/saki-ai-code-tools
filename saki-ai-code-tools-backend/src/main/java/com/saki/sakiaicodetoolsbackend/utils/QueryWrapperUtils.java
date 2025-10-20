package com.saki.sakiaicodetoolsbackend.utils;

import cn.hutool.core.util.StrUtil;

import java.util.function.Consumer;

/**
 * {@code QueryWrapperUtils} 是一个查询构建辅助工具类，
 * 主要用于在动态拼接 SQL 查询条件时，安全地添加非空或非空白字段条件。
 * <p>
 * 通常配合 MyBatis-Flex 的 {@code QueryWrapper} 使用，
 * 可避免无效或空值条件进入查询，提高代码可读性与健壮性。
 * </p>
 *
 * <p><strong>典型用法示例：</strong></p>
 * <pre>{@code
 * QueryWrapper query = QueryWrapper.create();
 * QueryWrapperUtils.applyIfNotBlank(userName, val -> query.and(User::getUserName).like(val));
 * QueryWrapperUtils.applyIfNotNull(userStatus, val -> query.and(User::getUserStatus).eq(val));
 * }</pre>
 *
 * @author saki酱
 * @version 1.0
 * @since 2025-10-19
 */
public class QueryWrapperUtils {
    /**
     * 当字符串 {@code value} 非空且非空白时，执行给定的操作。
     * <p>
     * 该方法在执行前会自动对字符串进行  去除首尾空格，
     * 常用于构建模糊查询或等值查询条件。
     * </p>
     *
     * @param value  待检查的字符串
     * @param action 当 {@code value} 非空且非空白时要执行的逻辑（例如添加查询条件）
     * @see StrUtil#isNotBlank(CharSequence)
     */
    public static void applyIfNotBlank(String value, Consumer<String> action) {
        if (StrUtil.isNotBlank(value)) {
            action.accept(StrUtil.trim(value));
        }
    }

    /**
     * 当对象 {@code value} 非 {@code null} 时，执行给定的操作。
     * <p>
     * 常用于数值型、枚举型、日期型等非字符串参数的动态条件构建。
     * </p>
     *
     * @param <T>    参数类型
     * @param value  待检查的对象
     * @param action 当 {@code value} 非 {@code null} 时要执行的逻辑（例如添加查询条件）
     */
    public static <T> void applyIfNotNull(T value, Consumer<T> action) {
        if (value != null) {
            action.accept(value);
        }
    }
}