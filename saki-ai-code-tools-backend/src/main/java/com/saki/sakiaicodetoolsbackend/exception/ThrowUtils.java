package com.saki.sakiaicodetoolsbackend.exception;

/**
 * 异常抛出工具类，提供简洁的条件判断和异常抛出方法。
 * 用于简化代码中的条件检查和异常抛出逻辑，提高代码可读性。
 *
 * <p>该类包含多个重载方法，支持不同的异常构造方式：
 * <ul>
 *   <li>直接抛出已有的运行时异常</li>
 *   <li>根据错误码枚举抛出业务异常</li>
 *   <li>根据错误码枚举和自定义消息抛出业务异常</li>
 * </ul>
 *
 * <p><b>使用示例：</b>
 * <pre>{@code
 * // 检查对象是否为null
 * ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
 *
 * // 检查字符串是否为空
 * ThrowUtils.throwIf(StringUtils.isBlank(username), ErrorCode.PARAMS_ERROR, "用户名不能为空");
 *
 * // 直接抛出特定异常
 * ThrowUtils.throwIf(condition, new IllegalArgumentException("参数不合法"));
 * }</pre>
 *
 *
 * @author sakisaki
 * @version 1.0
 * @since 2025-10-15
 */
public class ThrowUtils {
    /**
     * 私有构造方法，防止工具类被实例化。
     */
    private ThrowUtils() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    /**
     * 如果条件成立，则抛出指定的运行时异常。
     *
     * <p>该方法适用于需要直接抛出特定异常实例的场景。
     *
     * @param condition 布尔条件，当为true时抛出异常
     * @param runtimeException 要抛出的运行时异常实例，不能为null
     * @throws RuntimeException 当condition为true时，抛出传入的runtimeException
     * @throws NullPointerException 当runtimeException为null时抛出
     * @see RuntimeException
     */
    public static void throwIf(boolean condition, RuntimeException runtimeException) {
        if (runtimeException == null) {
            throw new NullPointerException("runtimeException不能为null");
        }
        if (condition) {
            throw runtimeException;
        }
    }

    /**
     * 如果条件成立，则根据错误码枚举抛出业务异常。
     *
     * <p>该方法使用错误码枚举中定义的默认消息构造业务异常。
     *
     * @param condition 布尔条件，当为true时抛出异常
     * @param errorCode 错误码枚举，用于构造业务异常，不能为null
     * @throws BusinessException 当condition为true时，抛出由errorCode构造的业务异常
     * @throws NullPointerException 当errorCode为null时抛出
     * @see BusinessException
     * @see ErrorCode
     */
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        throwIf(condition, new BusinessException(errorCode));
    }

    /**
     * 如果条件成立，则根据错误码枚举和自定义消息抛出业务异常。
     *
     * <p>该方法使用错误码枚举的错误码和自定义的消息构造业务异常。
     * 适用于需要覆盖默认错误消息或添加动态信息的场景。
     *
     * @param condition 布尔条件，当为true时抛出异常
     * @param errorCode 错误码枚举，用于获取错误码，不能为null
     * @param message 自定义错误消息，会覆盖错误码枚举中的默认消息
     * @throws BusinessException 当condition为true时，抛出由errorCode和message构造的业务异常
     * @throws NullPointerException 当errorCode为null时抛出
     * @see BusinessException
     * @see ErrorCode
     */
    public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        throwIf(condition, new BusinessException(errorCode, message));
    }
}
