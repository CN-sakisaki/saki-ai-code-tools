package com.saki.sakiaicodetoolsbackend.model.dto.admin.user;

import com.saki.sakiaicodetoolsbackend.common.DeleteRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 管理员删除用户请求对象
 * @author saki酱
 * @version 1.0
 * @since 2025-10-19
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "管理员删除用户请求对象")
public class UserDeleteRequest extends DeleteRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -3688458273073383536L;

}
