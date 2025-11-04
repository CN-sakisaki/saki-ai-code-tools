package com.saki.sakiaicodetoolsbackend.model.dto.app.user;

import com.saki.sakiaicodetoolsbackend.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户分页查询自己应用的请求对象。
 *
 * @author saki酱
 * @version 1.0
 * @since 2025-10-21
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "用户分页查询自己应用的请求对象")
public class AppMyListQueryRequest extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 7817400714629378908L;

    /**
     * 应用名称模糊查询。
     */
    @Schema(description = "应用名称模糊查询")
    private String appName;
}

