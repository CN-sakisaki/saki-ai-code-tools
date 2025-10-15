package com.saki.sakiaicodetoolsbackend.exception;

import com.saki.sakiaicodetoolsbackend.common.BaseResponse;
import com.saki.sakiaicodetoolsbackend.common.ResultUtils;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器，统一处理控制器层抛出的各种异常。
 * 将异常转换为标准化的API响应格式，避免敏感的异常信息直接暴露给客户端。
 *
 * <p>该处理器会捕获以下类型的异常：
 * <ul>
 *   <li>{@link BusinessException} - 业务异常，返回具体的错误码和消息</li>
 *   <li>{@link RuntimeException} - 运行时异常，返回系统错误信息</li>
 * </ul>
 *
 * <p><b>异常处理流程：</b>
 * <ol>
 *   <li>捕获控制器抛出的异常</li>
 *   <li>记录异常日志（包含堆栈信息）</li>
 *   <li>转换为统一的 {@link BaseResponse} 格式</li>
 *   <li>返回给客户端</li>
 * </ol>
 *
 * <p>使用 {@link Hidden} 注解表示该类不会在API文档中显示。
 *
 * @author sakisaki
 * @version 1.0
 * @since 2024-01-01
 * @see BusinessException
 * @see BaseResponse
 * @see ResultUtils
 * @see RestControllerAdvice
 * @see ExceptionHandler
 * @see Hidden
 */
@Hidden
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理业务异常。
     * 捕获 {@link BusinessException} 及其子类，返回具体的业务错误信息。
     *
     * <p><b>处理逻辑：</b>
     * <ul>
     *   <li>记录ERROR级别日志，包含异常堆栈信息</li>
     *   <li>使用异常中的错误码和消息构造失败响应</li>
     *   <li>返回标准化错误响应</li>
     * </ul>
     *
     * <p><b>返回示例：</b>
     * <pre>{@code
     * {
     *   "code": 40400,
     *   "data": null,
     *   "message": "请求数据不存在"
     * }
     * }</pre>
     *
     * @param e 业务异常实例，包含具体的错误码和消息
     * @return 标准化错误响应，包含业务错误码和消息
     * @see BusinessException
     * @see ResultUtils#error(int, String)
     */
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理运行时异常。
     * 捕获未被其他异常处理器处理的 {@link RuntimeException}，返回通用的系统错误信息。
     *
     * <p><b>处理逻辑：</b>
     * <ul>
     *   <li>记录ERROR级别日志，包含异常堆栈信息</li>
     *   <li>使用系统错误码和固定消息构造失败响应</li>
     *   <li>避免敏感的系统异常信息暴露给客户端</li>
     * </ul>
     *
     * <p><b>注意：</b>
     * 该方法会捕获所有未被前面异常处理器处理的运行时异常，
     * 返回统一的系统错误信息，避免泄露服务器内部细节。
     *
     * <p><b>返回示例：</b>
     * <pre>{@code
     * {
     *   "code": 50000,
     *   "data": null,
     *   "message": "系统错误"
     * }
     * }</pre>
     *
     * @param e 运行时异常实例
     * @return 标准化错误响应，包含系统错误码和固定消息
     * @see ErrorCode#SYSTEM_ERROR
     * @see ResultUtils#error(ErrorCode, String)
     */
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统内部异常");
    }
}