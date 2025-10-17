package com.saki.sakiaicodetoolsbackend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 刷新令牌请求。
 * @author saki酱
 * @version 1.0
 * @since 2025-10-17 11:38
 */
@Data
@Schema(description = "刷新令牌请求")
public class TokenRefreshRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 8430143008830980297L;

    /**
     * 过期或即将过期的 AccessToken。
     */
    @Schema(description = "AccessToken")
    private String accessToken;
}

