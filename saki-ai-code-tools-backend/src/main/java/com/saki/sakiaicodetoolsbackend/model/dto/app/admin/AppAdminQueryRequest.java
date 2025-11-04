package com.saki.sakiaicodetoolsbackend.model.dto.app.admin;

import com.saki.sakiaicodetoolsbackend.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 管理员分页查询应用请求对象。
 *
 * @author saki酱
 * @version 1.0
 * @since 2025-10-21
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "管理员分页查询应用请求对象")
public class AppAdminQueryRequest extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -4249844379338199305L;

    @Schema(description = "应用主键")
    private Long id;

    @Schema(description = "应用名称")
    private String appName;

    @Schema(description = "应用封面")
    private String cover;

    @Schema(description = "初始化 Prompt")
    private String initPrompt;

    @Schema(description = "代码生成类型")
    private String codeGenType;

    @Schema(description = "部署标识")
    private String deployKey;

    @Schema(description = "优先级")
    private Integer priority;

    @Schema(description = "创建用户 ID")
    private Long userId;

    @Schema(description = "是否删除标识（0-正常，1-删除）")
    private Integer isDelete;
}

