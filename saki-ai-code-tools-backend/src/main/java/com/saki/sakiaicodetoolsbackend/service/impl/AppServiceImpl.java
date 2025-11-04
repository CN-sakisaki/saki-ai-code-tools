package com.saki.sakiaicodetoolsbackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.saki.sakiaicodetoolsbackend.constant.AppConstants;
import com.saki.sakiaicodetoolsbackend.constant.AppFieldConstants;
import com.saki.sakiaicodetoolsbackend.constant.UserConstants;
import com.saki.sakiaicodetoolsbackend.exception.ErrorCode;
import com.saki.sakiaicodetoolsbackend.exception.ThrowUtils;
import com.saki.sakiaicodetoolsbackend.mapper.AppMapper;
import com.saki.sakiaicodetoolsbackend.model.dto.app.admin.AppAdminDeleteRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.app.admin.AppAdminQueryRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.app.admin.AppAdminUpdateRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.app.user.AppCreateRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.app.user.AppFeaturedListQueryRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.app.user.AppMyListQueryRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.app.user.AppUpdateRequest;
import com.saki.sakiaicodetoolsbackend.model.entity.App;
import com.saki.sakiaicodetoolsbackend.model.entity.User;
import com.saki.sakiaicodetoolsbackend.model.vo.AppVO;
import com.saki.sakiaicodetoolsbackend.service.AppService;
import com.saki.sakiaicodetoolsbackend.utils.QueryWrapperUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * <p>实现了普通用户及管理员的应用管理业务，包括创建、更新、删除、查询等功能。</p>
 *
 * @author saki酱
 * @since 2025-10-15
 */
@Service
@RequiredArgsConstructor
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    private static final String SORT_ORDER_ASC = "ascend";

    private static final Map<String, String> ADMIN_SORT_FIELD_MAP = Map.ofEntries(
            Map.entry("id", AppFieldConstants.ID),
            Map.entry("appName", AppFieldConstants.APP_NAME),
            Map.entry("cover", AppFieldConstants.COVER),
            Map.entry("codeGenType", AppFieldConstants.CODE_GEN_TYPE),
            Map.entry("deployKey", AppFieldConstants.DEPLOY_KEY),
            Map.entry("priority", AppFieldConstants.PRIORITY),
            Map.entry("userId", AppFieldConstants.USER_ID),
            Map.entry("isDelete", AppFieldConstants.IS_DELETE)
    );

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createApp(AppCreateRequest request, HttpServletRequest httpServletRequest) {
        validateRequest(request, "创建应用请求不能为空");
        User currentUser = getCurrentUser(httpServletRequest);

        String appName = StrUtil.trim(request.getAppName());
        String initPrompt = StrUtil.trim(request.getInitPrompt());

        ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_MISSING, "初始化 Prompt 不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(appName), ErrorCode.PARAMS_MISSING, "应用名称不能为空");

        App app = App.builder()
                .appName(appName)
                .cover(StrUtil.trimToNull(request.getCover()))
                .initPrompt(initPrompt)
                .codeGenType(StrUtil.trimToNull(request.getCodeGenType()))
                .priority(0)
                .userId(currentUser.getId())
                .editTime(LocalDateTime.now())
                .build();

        boolean saved = save(app);
        ThrowUtils.throwIf(!saved, ErrorCode.DATA_SAVE_FAILED, "创建应用失败");
        return app.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateMyApp(AppUpdateRequest request, HttpServletRequest httpServletRequest) {
        validateRequest(request, "更新应用请求不能为空");
        Long appId = request.getId();
        ThrowUtils.throwIf(appId == null, ErrorCode.PARAMS_MISSING, "应用 ID 不能为空");
        String appName = StrUtil.trim(request.getAppName());
        ThrowUtils.throwIf(StrUtil.isBlank(appName), ErrorCode.PARAMS_MISSING, "应用名称不能为空");

        User currentUser = getCurrentUser(httpServletRequest);
        App app = getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        ThrowUtils.throwIf(!Objects.equals(app.getUserId(), currentUser.getId()), ErrorCode.NO_AUTH_ERROR, "不能操作他人应用");

        boolean updated = UpdateChain.of(App.class)
                .set(App::getAppName, appName)
                .set(App::getEditTime, LocalDateTime.now())
                .where(App::getId).eq(appId)
                .and(App::getUserId).eq(currentUser.getId())
                .update();
        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "更新应用失败");
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteMyApp(Long id, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_MISSING, "应用 ID 不能为空");
        User currentUser = getCurrentUser(httpServletRequest);

        boolean removed = remove(QueryWrapper.create()
                .where(App::getId).eq(id)
                .and(App::getUserId).eq(currentUser.getId()));
        ThrowUtils.throwIf(!removed, ErrorCode.DATA_DELETE_FAILED, "删除应用失败");
        return Boolean.TRUE;
    }

    @Override
    public AppVO getMyAppDetail(Long id, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_MISSING, "应用 ID 不能为空");
        User currentUser = getCurrentUser(httpServletRequest);

        App app = getOne(QueryWrapper.create()
                .where(App::getId).eq(id)
                .and(App::getUserId).eq(currentUser.getId()));
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在或无权访问");
        return AppVO.fromEntity(app);
    }

    @Override
    public Page<AppVO> listMyAppsByPage(AppMyListQueryRequest request, HttpServletRequest httpServletRequest) {
        validateRequest(request, "分页查询请求不能为空");
        ThrowUtils.throwIf(request.getPageSize() > AppConstants.MAX_USER_PAGE_SIZE,
                ErrorCode.PARAMS_ERROR, "单页数量不能超过" + AppConstants.MAX_USER_PAGE_SIZE + "条");
        User currentUser = getCurrentUser(httpServletRequest);

        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(App::getIsDelete).eq(0)
                .and(App::getUserId).eq(currentUser.getId());
        QueryWrapperUtils.applyIfNotBlank(request.getAppName(),
                val -> queryWrapper.and(App::getAppName).like(val));

        queryWrapper.orderBy(AppConstants.DEFAULT_SORT_COLUMN, false)
                .orderBy(AppConstants.SECONDARY_SORT_COLUMN, false);

        Page<App> page = page(new Page<>(request.getPageNum(), request.getPageSize()), queryWrapper);
        return convertPage(page, AppVO::fromEntity);
    }

    @Override
    public Page<AppVO> listFeaturedAppsByPage(AppFeaturedListQueryRequest request) {
        validateRequest(request, "分页查询请求不能为空");
        ThrowUtils.throwIf(request.getPageSize() > AppConstants.MAX_USER_PAGE_SIZE,
                ErrorCode.PARAMS_ERROR, "单页数量不能超过" + AppConstants.MAX_USER_PAGE_SIZE + "条");

        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(App::getIsDelete).eq(0)
                .and(App::getPriority).ge(AppConstants.FEATURED_PRIORITY_THRESHOLD);
        QueryWrapperUtils.applyIfNotBlank(request.getAppName(),
                val -> queryWrapper.and(App::getAppName).like(val));

        queryWrapper.orderBy(AppConstants.DEFAULT_SORT_COLUMN, false)
                .orderBy(AppConstants.SECONDARY_SORT_COLUMN, false);

        Page<App> page = page(new Page<>(request.getPageNum(), request.getPageSize()), queryWrapper);
        return convertPage(page, AppVO::fromEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean adminDeleteApps(AppAdminDeleteRequest request) {
        validateRequest(request, "删除请求不能为空");
        List<Long> ids = request.getIds();
        ThrowUtils.throwIf(CollUtil.isEmpty(ids), ErrorCode.PARAMS_MISSING, "删除的应用 ID 不能为空");

        boolean removed = removeByIds(ids);
        ThrowUtils.throwIf(!removed, ErrorCode.DATA_DELETE_FAILED, "删除应用失败");
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean adminUpdateApp(AppAdminUpdateRequest request) {
        validateRequest(request, "更新请求不能为空");
        Long id = request.getId();
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_MISSING, "应用 ID 不能为空");

        App app = getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        String appName = StrUtil.trimToNull(request.getAppName());
        String cover = StrUtil.trimToNull(request.getCover());
        Integer priority = request.getPriority();
        ThrowUtils.throwIf(appName == null && cover == null && priority == null,
                ErrorCode.PARAMS_ERROR, "请至少提供一个更新字段");

        UpdateChain<App> chain = UpdateChain.of(App.class)
                .where(App::getId).eq(id);
        if (appName != null) {
            chain.set(App::getAppName, appName);
        }
        if (cover != null) {
            chain.set(App::getCover, cover);
        }
        if (priority != null) {
            chain.set(App::getPriority, priority);
        }
        chain.set(App::getEditTime, LocalDateTime.now());

        boolean updated = chain.update();
        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "更新应用失败");
        return Boolean.TRUE;
    }

    @Override
    public Page<App> adminListAppsByPage(AppAdminQueryRequest request) {
        validateRequest(request, "分页查询请求不能为空");

        QueryWrapper queryWrapper = QueryWrapper.create().where(App::getIsDelete).ge(0);
        QueryWrapperUtils.applyIfNotNull(request.getId(), val -> queryWrapper.and(App::getId).eq(val));
        QueryWrapperUtils.applyIfNotBlank(request.getAppName(),
                val -> queryWrapper.and(App::getAppName).like(val));
        QueryWrapperUtils.applyIfNotBlank(request.getCover(),
                val -> queryWrapper.and(App::getCover).like(val));
        QueryWrapperUtils.applyIfNotBlank(request.getInitPrompt(),
                val -> queryWrapper.and(App::getInitPrompt).like(val));
        QueryWrapperUtils.applyIfNotBlank(request.getCodeGenType(),
                val -> queryWrapper.and(App::getCodeGenType).eq(val));
        QueryWrapperUtils.applyIfNotBlank(request.getDeployKey(),
                val -> queryWrapper.and(App::getDeployKey).eq(val));
        QueryWrapperUtils.applyIfNotNull(request.getPriority(),
                val -> queryWrapper.and(App::getPriority).eq(val));
        QueryWrapperUtils.applyIfNotNull(request.getUserId(),
                val -> queryWrapper.and(App::getUserId).eq(val));
        QueryWrapperUtils.applyIfNotNull(request.getIsDelete(),
                val -> queryWrapper.and(App::getIsDelete).eq(val));

        String sortField = StrUtil.trimToNull(request.getSortField());
        String sortOrder = StrUtil.trimToNull(request.getSortOrder());
        String sortColumn = sortField == null ? null : ADMIN_SORT_FIELD_MAP.get(sortField);
        boolean asc = StrUtil.equalsIgnoreCase(sortOrder, SORT_ORDER_ASC);

        if (sortColumn != null) {
            queryWrapper.orderBy(sortColumn, asc);
        } else {
            queryWrapper.orderBy(AppConstants.DEFAULT_SORT_COLUMN, false)
                    .orderBy(AppConstants.SECONDARY_SORT_COLUMN, false);
        }

        Page<App> page = new Page<>(request.getPageNum(), request.getPageSize());
        return page(page, queryWrapper);
    }

    @Override
    public App getAdminAppDetail(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_MISSING, "应用 ID 不能为空");
        App app = getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        return app;
    }

    private User getCurrentUser(HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(httpServletRequest == null, ErrorCode.NOT_LOGIN_ERROR, "未检测到登录用户");
        HttpSession session = httpServletRequest.getSession(false);
        ThrowUtils.throwIf(session == null, ErrorCode.NOT_LOGIN_ERROR, "未登录或会话已失效");
        Object userObj = session.getAttribute(UserConstants.USER_LOGIN_STATE);
        ThrowUtils.throwIf(!(userObj instanceof User), ErrorCode.NOT_LOGIN_ERROR, "未登录或会话已失效");
        return (User) userObj;
    }

    private void validateRequest(Object request, String message) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_MISSING, message);
    }

    private <T, R> Page<R> convertPage(Page<T> source, Function<T, R> mapper) {
        Page<R> target = new Page<>(source.getPageNumber(), source.getPageSize(), source.getTotalRow());
        List<T> originRecords = source.getRecords();
        List<R> records = originRecords == null ? new ArrayList<>() : originRecords.stream()
                .map(mapper)
                .collect(Collectors.toList());
        target.setRecords(records);
        return target;
    }
}

