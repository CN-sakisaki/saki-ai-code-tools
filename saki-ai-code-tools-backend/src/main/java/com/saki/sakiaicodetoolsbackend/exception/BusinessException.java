package com.saki.sakiaicodetoolsbackend.exception;

import lombok.Getter;

/**
 * 自定义业务异常类，用于处理系统业务逻辑中的异常情况。
 * 与Java内置异常类区分，提供错误码和错误信息的统一封装。
 *
 * @author sakisaki
 * @version 1.0
 * @since 2024-01-01
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final int code;

    /**
     * 使用指定的错误码和错误信息构造业务异常。
     *
     * @param code 错误码，用于标识错误类型
     * @param message 错误描述信息，用于说明具体的错误原因
     * @throws IllegalArgumentException 当错误码为负数或错误信息为空时抛出
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 使用错误码枚举构造业务异常。
     * 错误信息从错误码枚举中获取。
     *
     * @param errorCode 错误码枚举实例，不能为null
     * @throws NullPointerException 当errorCode为null时抛出
     * @see ErrorCode
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    /**
     * 使用错误码枚举和自定义错误信息构造业务异常。
     * 错误码使用枚举中的定义，错误信息使用自定义内容。
     *
     * @param errorCode 错误码枚举实例，不能为null
     * @param message 自定义错误描述信息，会覆盖枚举中的默认信息
     * @throws NullPointerException 当errorCode为null时抛出
     * @see ErrorCode
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }
}

