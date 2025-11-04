package com.saki.sakiaicodetoolsbackend.model.dto.app.user;

import com.saki.sakiaicodetoolsbackend.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 精选应用列表查询请求对象。
 *
 * @author saki酱
 * @version 1.0
 * @since 2025-10-21
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "精选应用列表查询请求对象")
public class AppFeaturedListQueryRequest extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -6021623997951800059L;

    /**
     * 应用名称模糊查询。
     */
    @Schema(description = "应用名称模糊查询")
    private String appName;
}

