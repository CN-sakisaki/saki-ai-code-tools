package com.saki.sakiaicodetoolsbackend.model.dto.app.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 管理员更新应用请求对象。
 *
 * @author saki酱
 * @version 1.0
 * @since 2025-10-21
 */
@Data
@Schema(description = "管理员更新应用请求对象")
public class AppAdminUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -3557720914245399648L;

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

    /**
     * 应用封面。
     */
    @Schema(description = "应用封面")
    private String cover;

    /**
     * 应用优先级。
     */
    @Schema(description = "应用优先级")
    private Integer priority;
}

