package com.saki.sakiaicodetoolsbackend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 刷新令牌请求。
 */
@Data
@Schema(description = "刷新令牌请求")
public class TokenRefreshRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 过期或即将过期的 AccessToken。
     */
    @Schema(description = "AccessToken")
    private String accessToken;
}

