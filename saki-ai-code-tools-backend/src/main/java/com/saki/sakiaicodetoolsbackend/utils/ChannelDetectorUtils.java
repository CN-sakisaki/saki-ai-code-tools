package com.saki.sakiaicodetoolsbackend.utils;

import com.saki.sakiaicodetoolsbackend.exception.BusinessException;
import com.saki.sakiaicodetoolsbackend.exception.ErrorCode;

import java.util.regex.Pattern;

/**
 * 渠道识别工具类
 * 用于判断用户输入是邮箱还是手机号。
 *
 * <p>典型用途：
 * <ul>
 *     <li>验证码发送接口自动识别发送渠道</li>
 *     <li>登录/注册场景动态分发策略</li>
 * </ul>
 *
 * @author saki
 * @version 1.1
 * @since 2025-10-23
 */
public class ChannelDetectorUtils {

    /** 邮箱正则 */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    /** 手机号正则（中国大陆） */
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^1((3[0-9])|(4[5-9])|(5[0-35-9])|(6[5-7])|(7[0-8])|(8[0-9])|(9[189]))\\d{8}$");

    private ChannelDetectorUtils() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    /**
     * 自动识别渠道
     *
     * @param receiver 用户输入的接收者（邮箱或手机号）
     * @return 渠道类型："EMAIL" 或 "PHONE"
     * @throws BusinessException 输入格式错误
     */
    public static String detectChannel(String receiver) {
        if (receiver == null || receiver.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_MISSING, "接收者不能为空");
        }

        String input = receiver.trim();
        if (EMAIL_PATTERN.matcher(input).matches()) {
            return "EMAIL";
        } else if (PHONE_PATTERN.matcher(input).matches()) {
            return "PHONE";
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请输入正确的邮箱或手机号");
        }
    }
}
