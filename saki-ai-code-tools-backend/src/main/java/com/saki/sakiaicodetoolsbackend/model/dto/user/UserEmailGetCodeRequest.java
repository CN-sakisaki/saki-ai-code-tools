package com.saki.sakiaicodetoolsbackend.model.dto.user;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 *  根据邮箱发送验证码请求对象
 * @author saki酱
 * @version 1.0
 * @since 2025-10-20
 */
@Data
public class UserEmailGetCodeRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 5903650286098282614L;

    @Schema(description = "邮箱")
    private String email;
}
