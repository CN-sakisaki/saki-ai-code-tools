package com.saki.sakiaicodetoolsbackend.common;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 批量删除请求参数封装类。
 * 用于接收前端传递的批量删除操作所需的ID列表，支持多种业务实体的批量删除。
 *
 * @author sakisaki
 * @version 1.0
 * @since 2025-10-15 14:36
 */
@Data
public class DeleteRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 5360265500191891943L;

    private List<Long> ids;
}
