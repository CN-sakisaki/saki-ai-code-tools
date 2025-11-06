package com.saki.sakiaicodetoolsbackend.model.dto.app.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户更新自己的应用请求体。
 *
 * @author saki酱
 * @version 1.0
 * @since 2025-10-21
 */
@Data
@Schema(description = "用户更新自己的应用请求体")
public class AppUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -6872512230561122159L;

    /**
     * 应用主键。
     */
    @Schema(description = "应用主键")
    private Long id;

    /**
     * 应用名称。
     */
    @Schema(description = "应用名称")
    private String appName;
}

