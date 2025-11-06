package com.saki.sakiaicodetoolsbackend.constant;

/**
 * 应用模块常量定义。
 *
 * <p>用于集中维护应用模块的常量，避免魔法值分散在业务代码中。</p>
 *
 * @author saki酱
 * @version 1.0
 * @since 2025-10-21
 */
public final class AppConstants {

    private AppConstants() {
    }

    /**
     * 普通用户分页查询时允许的最大页大小。
     */
    public static final int MAX_USER_PAGE_SIZE = 20;

    /**
     * 精选应用优先级阈值。
     */
    public static final int FEATURED_PRIORITY_THRESHOLD = 1;

    /**
     * 默认排序字段，优先级倒序。
     */
    public static final String DEFAULT_SORT_COLUMN = "priority";

    /**
     * 次级排序字段，更新时间倒序。
     */
    public static final String SECONDARY_SORT_COLUMN = "update_time";

    /**
     * 应用生成目录
     */
    public static final String CODE_OUTPUT_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

    /**
     * 应用部署目录
     */
    public static final String CODE_DEPLOY_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_deploy";

    /**
     * 应用部署域名
     */
    public static final String CODE_DEPLOY_HOST = "http://localhost";

}
