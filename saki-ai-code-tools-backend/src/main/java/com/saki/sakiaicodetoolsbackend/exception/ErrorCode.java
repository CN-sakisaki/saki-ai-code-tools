package com.saki.sakiaicodetoolsbackend.exception;

import lombok.Getter;

/**
 * 错误码枚举类
 * @author sakisaki
 * @version 1.0
 * @since 2025/10/14 22:51
 */
@Getter
public enum ErrorCode {

    SUCCESS(0, "ok"),

    PARAMS_ERROR(40000, "请求参数错误"),
    PARAMS_MISSING(40001, "缺少必要参数"),
    PARAMS_FORMAT_ERROR(40002, "参数格式不正确"),
    SIGNATURE_ERROR(40003, "请求签名错误"),
    UNSUPPORTED_METHOD(40004, "请求方式不支持"),
    REQUEST_TOO_FREQUENT(40005, "请求过于频繁"),

    NOT_LOGIN_ERROR(40100, "未登录"),
    NO_AUTH_ERROR(40101, "无权限"),
    TOKEN_INVALID(40102, "登录凭证无效"),
    ACCOUNT_LOCKED(40103, "账号被锁定"),
    ACCOUNT_DISABLED(40104, "账号被禁用"),
    LOGIN_EXPIRED(40106, "登录状态已过期，需要重新登录"),

    FORBIDDEN_ERROR(40300, "禁止访问"),
    OPERATION_NOT_ALLOWED(40301, "不允许的操作"),
    ROLE_LIMITED(40302, "当前角色无权限操作"),
    RESOURCE_LOCKED(40304, "资源被占用或锁定"),

    NOT_FOUND_ERROR(40400, "请求数据不存在"),
    FILE_NOT_FOUND(40401, "文件不存在"),
    DATA_CONFLICT(40402, "数据冲突"),
    DATA_ALREADY_EXISTS(40403, "数据已存在"),
    DATA_SAVE_FAILED(40404, "数据保存失败"),
    DATA_DELETE_FAILED(40405, "删除失败"),


    SYSTEM_ERROR(50000, "系统内部异常"),
    OPERATION_ERROR(50001, "操作失败"),
    SERVICE_UNAVAILABLE(50002, "服务暂不可用"),
    DATABASE_ERROR(50003, "数据库错误"),
    NETWORK_ERROR(50004, "网络异常"),
    TIMEOUT_ERROR(50004, "请求超时"),

    EXTERNAL_SERVICE_ERROR(70000, "外部服务调用失败"),
    SMS_SEND_FAILED(70002, "短信发送失败"),
    EMAIL_SEND_FAILED(70003, "邮件发送失败");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
