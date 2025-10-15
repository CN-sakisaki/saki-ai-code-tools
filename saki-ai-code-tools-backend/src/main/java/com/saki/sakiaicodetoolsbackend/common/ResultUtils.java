package com.saki.sakiaicodetoolsbackend.common;

import com.saki.sakiaicodetoolsbackend.exception.ErrorCode;

/**
 * 统一响应结果工具类，提供快速构建成功和失败响应的方法。
 * 用于标准化API接口的返回格式，提高代码的简洁性和一致性。
 *
 * <p>该类封装了常见地响应构造场景，包括：
 * <ul>
 *   <li>成功响应（带数据）</li>
 *   <li>基于错误码枚举的失败响应</li>
 *   <li>基于错误码和自定义消息的失败响应</li>
 *   <li>直接指定错误码和消息的失败响应</li>
 * </ul>
 *
 * @author sakisaki
 * @version 1.0
 * @since 2025-10-15 13:41
 */
public class ResultUtils {

    /**
     * 私有构造方法，防止工具类被实例化。
     */
    private ResultUtils() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    /**
     * 构建成功响应结果。
     * 响应码为0，消息为"ok"，并包含返回的数据。
     *
     * <p><b>示例：</b>
     * <pre>{@code
     *
     * // 返回结果：
     * // {
     * //   "code": 0,
     * //   "data": { ... },
     * //   "message": "ok"
     * // }
     * }</pre>
     *
     * @param <T> 数据的类型
     * @param data 要返回的数据，可以为null
     * @return 成功响应对象，包含数据和默认的成功信息
     * @see BaseResponse
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok");
    }

    /**
     * 构建失败响应结果。
     * 使用错误码枚举中的错误码和默认消息。
     *
     * <p><b>示例：</b>
     * <pre>{@code
     * if (user == null) {
     *     return ResultUtils.error(ErrorCode.NOT_FOUND_ERROR);
     * }
     * // 返回结果：
     * // {
     * //   "code": 40400,
     * //   "data": null,
     * //   "message": "请求数据不存在"
     * // }
     * }</pre>
     *
     * @param errorCode 错误码枚举，不能为null
     * @return 失败响应对象，包含错误码和对应的错误消息
     * @throws NullPointerException 当errorCode为null时抛出
     * @see ErrorCode
     * @see BaseResponse
     */
    public static BaseResponse<?> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     * 构建失败响应结果。
     * 直接指定错误码和错误消息。
     *
     * <p><b>示例：</b>
     * <pre>{@code
     * return ResultUtils.error(50000, "系统内部错误");
     *
     * // 返回结果：
     * // {
     * //   "code": 50000,
     * //   "data": null,
     * //   "message": "系统内部错误"
     * // }
     * }</pre>
     *
     * @param code 错误码，通常为非零值
     * @param message 错误描述信息
     * @return 失败响应对象，包含指定的错误码和错误消息
     * @see BaseResponse
     */
    public static BaseResponse<?> error(int code, String message) {
        return new BaseResponse<>(code, null, message);
    }

    /**
     * 构建失败响应结果。
     * 使用错误码枚举的错误码，但覆盖其默认消息。
     *
     * <p><b>示例：</b>
     * <pre>{@code
     * return ResultUtils.error(ErrorCode.PARAMS_ERROR, "用户名长度必须在2-20字符之间");
     *
     * // 返回结果：
     * // {
     * //   "code": 40000,
     * //   "data": null,
     * //   "message": "用户名长度必须在2-20字符之间"
     * // }
     * }</pre>
     *
     * @param errorCode 错误码枚举，用于获取错误码，不能为null
     * @param message 自定义错误消息，会覆盖错误码枚举的默认消息
     * @return 失败响应对象，包含错误码和自定义的错误消息
     * @throws NullPointerException 当errorCode为null时抛出
     * @see ErrorCode
     * @see BaseResponse
     */
    public static BaseResponse<?> error(ErrorCode errorCode, String message) {
        return new BaseResponse<>(errorCode.getCode(), null, message);
    }
}

