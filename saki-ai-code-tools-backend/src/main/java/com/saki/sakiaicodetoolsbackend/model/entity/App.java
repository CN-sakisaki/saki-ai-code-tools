package com.saki.sakiaicodetoolsbackend.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 应用 实体类。
 *
 * @author saki酱
 * @since 2025-10-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "应用")
@Table(value = "app", schema = "saki_ai_code_tools")
public class App implements Serializable {

    @Serial
    private static final long serialVersionUID = -1420495993920615460L;

    /**
     * 主键
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    @Schema(description = "主键")
    private Long id;

    /**
     * 应用名称
     */
    @Schema(description = "应用名称")
    private String appName;

    /**
     * 应用封面
     */
    @Schema(description = "应用封面")
    private String cover;

    /**
     * 应用初始化的 prompt
     */
    @Schema(description = "应用初始化的 prompt")
    private String initPrompt;

    /**
     * 代码生成类型（枚举）
     */
    @Schema(description = "代码生成类型（枚举）")
    private String codeGenType;

    /**
     * 部署标识
     */
    @Schema(description = "部署标识")
    private String deployKey;

    /**
     * 部署时间
     */
    @Schema(description = "部署时间")
    private LocalDateTime deployedTime;

    /**
     * 优先级
     */
    @Schema(description = "优先级")
    private Integer priority;

    /**
     * 创建用户 ID
     */
    @Schema(description = "创建用户 ID")
    private Long userId;

    /**
     * 编辑时间
     */
    @Schema(description = "编辑时间")
    private LocalDateTime editTime;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除（0 - 未删除，1 - 删除）
     */
    @Schema(description = "逻辑删除（0 - 未删除，1 - 删除）")
    @Column(isLogicDelete = true)
    private Integer isDelete;

}
