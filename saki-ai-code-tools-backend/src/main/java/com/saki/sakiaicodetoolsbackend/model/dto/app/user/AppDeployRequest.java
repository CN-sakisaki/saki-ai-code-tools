package com.saki.sakiaicodetoolsbackend.model.dto.app.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 *
 * @author saki酱
 * @version 1.0
 * @since 2025-11-07
 */
@Data
public class AppDeployRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 48116568223962387L;

    /**
     * 应用Id
     */
    @Schema(description = "应用Id")
    private Long appId;
}
