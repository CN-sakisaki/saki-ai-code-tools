package com.saki.sakiaicodetoolsbackend.common;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 分页查询请求参数封装类。
 * 用于接收前端传递的分页查询参数，支持排序和通用的分页需求。
 *
 * @author sakisaki
 * @version 1.0
 * @since 2025-10-15 14:38
 */
@Data
public class PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 3293004527593809311L;

    /**
     * 当前页码。
     */
    private int pageNum = 1;

    /**
     * 每页记录数。
     */
    private int pageSize = 10;

    /**
     * 排序字段
     *
     * <p><b>使用说明：</b>
     * <ul>
     *   <li>对应数据库字段名（如：create_time）</li>
     *   <li>或对应实体类字段名（如：createTime）</li>
     *   <li>为空时不进行排序</li>
     * </ul>
     *
     * <p><b>安全建议：</b>
     * 在实际使用中应该对排序字段进行白名单验证，防止SQL注入。
     *
     * <p><b>示例：</b>
     * <pre>{@code
     * // 正常情况
     * ORDER BY create_time DESC
     *
     * // 恶意情况：前端传入 sortField = "id; DROP TABLE users "
     * ORDER BY id; DROP TABLE users -- DESC
     * }</pre>
     *
     */
    private String sortField;

    /**
     * 排序顺序（默认降序）
     * <p><b>可选值：</b>
     * <ul>
     *   <li>ascend - 升序（从小到大）</li>
     *   <li>descend - 降序（从大到小）</li>
     * </ul>
     */
    private String sortOrder = "descend";

}

