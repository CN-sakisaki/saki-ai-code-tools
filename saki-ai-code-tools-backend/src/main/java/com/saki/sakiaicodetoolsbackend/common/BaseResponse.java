package com.saki.sakiaicodetoolsbackend.common;

import com.saki.sakiaicodetoolsbackend.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一API响应封装类，用于标准化所有接口的返回格式。
 * 采用泛型设计，支持不同类型的数据返回，确保前后端数据交互的一致性。
 *
 * <p>响应结构说明：
 * <ul>
 *   <li><b>code</b> - 状态码：0表示成功，非0表示失败</li>
 *   <li><b>data</b> - 响应数据：成功时返回的业务数据</li>
 *   <li><b>message</b> - 响应消息：成功时为"ok"，失败时为错误描述</li>
 * </ul>
 *
 * <p><b>成功响应示例：</b>
 * <pre>{@code
 * {
 *   "code": 0,
 *   "data": {
 *     "id": 1,
 *     "name": "张三"
 *   },
 *   "message": "ok"
 * }
 * }</pre>
 *
 * <p><b>失败响应示例：</b>
 * <pre>{@code
 * {
 *   "code": 40400,
 *   "data": null,
 *   "message": "请求数据不存在"
 * }
 * }</pre>
 *
 * @param <T> 响应数据的类型
 * @author sakisaki
 * @version 1.0
 * @since 2024-10-15
 * @see ResultUtils
 * @see ErrorCode
 * @see Serializable
 */
@Data
public class BaseResponse<T> implements Serializable {
    /**
     * 响应状态码。
     *
     * <p>状态码约定：
     * <ul>
     *   <li>0 - 成功</li>
     *   <li>50000+ - 系统保留错误码</li>
     *   <li>40000+ - 业务错误码</li>
     * </ul>
     */
    private int code;

    /**
     * 响应数据。
     *
     * <p>成功时包含业务数据，失败时为null。
     * 使用泛型确保类型安全，支持任意类型的业务数据返回。
     */
    private T data;

    /**
     * 响应消息。
     *
     * <p>成功时默认为"ok"，失败时为具体的错误描述信息。
     * 用于前端展示和调试信息。
     */
    private String message;

    /**
     * 构造完整的响应对象。
     *
     * @param code 响应状态码
     * @param data 响应数据
     * @param message 响应消息
     */
    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    /**
     * 构造响应对象（无消息）。
     * 消息字段默认为空字符串。
     *
     * @param code 响应状态码
     * @param data 响应数据
     */
    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    /**
     * 根据错误码枚举构造失败响应对象。
     * 自动使用错误码枚举中的状态码和默认消息。
     *
     * @param errorCode 错误码枚举，不能为null
     * @throws NullPointerException 当errorCode为null时抛出
     * @see ErrorCode
     */
    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}

