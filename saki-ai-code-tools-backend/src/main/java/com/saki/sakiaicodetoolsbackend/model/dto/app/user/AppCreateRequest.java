package com.saki.sakiaicodetoolsbackend.model.dto.app.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户创建应用请求体。
 *
 * @author saki酱
 * @version 1.0
 * @since 2025-10-21
 */
@Data
@Schema(description = "用户创建应用请求体")
public class AppCreateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -4481639892988943252L;

    /**
     * 应用名称。
     */
    @Schema(description = "应用名称")
    private String appName;

    /**
     * 应用封面链接。
     */
    @Schema(description = "应用封面链接")
    private String cover;

    /**
     * 初始化 Prompt，必填。
     */
    @Schema(description = "初始化 Prompt，必填")
    private String initPrompt;

    /**
     * 代码生成类型。
     */
    @Schema(description = "代码生成类型")
    private String codeGenType;
}

