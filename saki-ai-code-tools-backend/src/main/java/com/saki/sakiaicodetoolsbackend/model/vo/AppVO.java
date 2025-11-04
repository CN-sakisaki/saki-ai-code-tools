package com.saki.sakiaicodetoolsbackend.model.vo;

import cn.hutool.core.bean.BeanUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 应用视图对象，用于接口层返回。
 *
 * @author saki酱
 * @version 1.0
 * @since 2025-10-21
 */
@Data
@Schema(description = "应用视图对象")
public class AppVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -9119736654123216115L;

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "应用名称")
    private String appName;

    @Schema(description = "应用封面")
    private String cover;

    @Schema(description = "应用初始化 Prompt")
    private String initPrompt;

    @Schema(description = "代码生成类型")
    private String codeGenType;

    @Schema(description = "部署标识")
    private String deployKey;

    @Schema(description = "部署时间")
    private LocalDateTime deployedTime;

    @Schema(description = "优先级")
    private Integer priority;

    @Schema(description = "创建用户 ID")
    private Long userId;

    @Schema(description = "编辑时间")
    private LocalDateTime editTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 将实体对象转换为视图对象。
     *
     * @param app 实体对象
     * @return 视图对象
     */
    public static AppVO fromEntity(com.saki.sakiaicodetoolsbackend.model.entity.App app) {
        if (app == null) {
            return null;
        }
        AppVO vo = new AppVO();
        BeanUtil.copyProperties(app, vo);
        return vo;
    }
}

