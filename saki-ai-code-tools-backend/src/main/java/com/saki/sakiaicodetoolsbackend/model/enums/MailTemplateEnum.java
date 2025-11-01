package com.saki.sakiaicodetoolsbackend.model.enums;


import com.saki.sakiaicodetoolsbackend.exception.BusinessException;
import com.saki.sakiaicodetoolsbackend.exception.ErrorCode;
import lombok.Getter;

/**
 * 负责描述各种邮件类型的 主题 和 模板路径
 * @author saki酱
 * @version 1.0
 * @since 2025-10-23
 */
@Getter
public enum MailTemplateEnum {

    /** 登录验证码 */
    LOGIN_CODE("登录验证码", "登录账号", "templates/universal-code.html"),

    /** 注册验证码 */
    REGISTER_CODE("注册验证码", "注册账号", "templates/universal-code.html"),

    /** 修改邮箱验证码 */
    UPDATE_EMAIL_CODE("邮箱验证", "修改邮箱", "templates/universal-code.html"),

    /** 重置密码验证码 */
    RESET_PASSWORD_CODE("重置密码", "重置密码", "templates/universal-code.html");

    /** 邮件主题 */
    private final String subject;

    /** 操作描述（模板中 ${action}） */
    private final String action;

    /**
     * 邮件模版路径
     */
    private final String templatePath;

    MailTemplateEnum(String subject, String action, String templatePath) {
        this.subject = subject;
        this.action = action;
        this.templatePath = templatePath;
    }

    /**
     * 根据 scene 字段动态解析模板类型
     *
     * @param scene 前端传的字符串（不区分大小写）
     * @return 对应的 MailTemplateEnum
     */
    public static MailTemplateEnum fromScene(String scene) {
        if (scene == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "scene 不能为空");
        }

        return switch (scene.toUpperCase()) {
            case "LOGIN" -> LOGIN_CODE;
            case "REGISTER" -> REGISTER_CODE;
            case "UPDATE_EMAIL" -> UPDATE_EMAIL_CODE;
            case "RESET_PASSWORD" -> RESET_PASSWORD_CODE;
            default -> throw new BusinessException(ErrorCode.PARAMS_ERROR, "未知的验证码场景: " + scene);
        };
    }
}
