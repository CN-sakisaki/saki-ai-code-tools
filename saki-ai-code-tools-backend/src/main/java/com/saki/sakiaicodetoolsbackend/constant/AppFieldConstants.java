package com.saki.sakiaicodetoolsbackend.constant;

/**
 * 应用表字段常量，统一维护数据库字段名称。
 *
 * <p>推荐在构建查询和排序语句时使用该类中的常量，以提升代码可维护性。</p>
 *
 * @author saki酱
 * @version 1.0
 * @since 2025-10-21
 */
public final class AppFieldConstants {

    private AppFieldConstants() {
    }

    public static final String ID = "id";
    public static final String APP_NAME = "app_name";
    public static final String COVER = "cover";
    public static final String INIT_PROMPT = "init_prompt";
    public static final String CODE_GEN_TYPE = "code_gen_type";
    public static final String DEPLOY_KEY = "deploy_key";
    public static final String DEPLOYED_TIME = "deployed_time";
    public static final String PRIORITY = "priority";
    public static final String USER_ID = "user_id";
    public static final String EDIT_TIME = "edit_time";
    public static final String CREATE_TIME = "create_time";
    public static final String UPDATE_TIME = "update_time";
    public static final String IS_DELETE = "is_delete";
}
