package com.saki.sakiaicodetoolsbackend.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.saki.sakiaicodetoolsbackend.model.dto.app.admin.AppAdminDeleteRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.app.admin.AppAdminQueryRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.app.admin.AppAdminUpdateRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.app.user.AppCreateRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.app.user.AppFeaturedListQueryRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.app.user.AppMyListQueryRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.app.user.AppUpdateRequest;
import com.saki.sakiaicodetoolsbackend.model.entity.App;
import com.saki.sakiaicodetoolsbackend.model.vo.AppVO;
import com.saki.sakiaicodetoolsbackend.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import reactor.core.publisher.Flux;

/**
 * 应用 服务层，负责处理应用模块的业务逻辑。
 *
 * <p>包含用户与管理员的应用管理操作，例如创建、更新、删除、分页查询等。</p>
 *
 * @author saki酱
 * @since 2025-10-15
 */
public interface AppService extends IService<App> {

    /**
     * 当前登录用户创建应用。
     *
     * @param request             创建请求参数
     * @param httpServletRequest  请求上下文，用于获取登录态
     * @return 新创建的应用 ID
     */
    Long createApp(AppCreateRequest request, HttpServletRequest httpServletRequest);

    /**
     * 当前登录用户更新自己的应用，仅支持更新名称。
     *
     * @param request             更新请求参数
     * @param httpServletRequest  请求上下文
     * @return 是否更新成功
     */
    Boolean updateMyApp(AppUpdateRequest request, HttpServletRequest httpServletRequest);

    /**
     * 当前登录用户删除自己的应用。
     *
     * @param id                  应用主键
     * @param httpServletRequest  请求上下文
     * @return 是否删除成功
     */
    Boolean deleteMyApp(Long id, HttpServletRequest httpServletRequest);

    /**
     * 当前登录用户查看自己的应用详情。
     *
     * @param id                  应用主键
     * @param httpServletRequest  请求上下文
     * @return 应用详情
     */
    AppVO getMyAppDetail(Long id, HttpServletRequest httpServletRequest);

    /**
     * 当前登录用户分页查询自己的应用列表。
     *
     * @param request             查询请求参数
     * @param httpServletRequest  请求上下文
     * @return 应用分页数据
     */
    Page<AppVO> listMyAppsByPage(AppMyListQueryRequest request, HttpServletRequest httpServletRequest);

    /**
     * 分页查询精选应用列表。
     *
     * @param request 查询请求参数
     * @return 精选应用分页数据
     */
    Page<AppVO> listFeaturedAppsByPage(AppFeaturedListQueryRequest request);

    /**
     * 管理员批量删除应用。
     *
     * @param request 删除请求参数
     * @return 是否删除成功
     */
    Boolean adminDeleteApps(AppAdminDeleteRequest request);

    /**
     * 管理员更新应用信息。
     *
     * @param request 更新请求参数
     * @return 是否更新成功
     */
    Boolean adminUpdateApp(AppAdminUpdateRequest request);

    /**
     * 管理员分页查询应用列表。
     *
     * @param request 查询请求参数
     * @return 应用分页数据
     */
    Page<App> adminListAppsByPage(AppAdminQueryRequest request);

    /**
     * 管理员根据主键查看应用详情。
     *
     * @param id 应用主键
     * @return 应用详情
     */
    App getAdminAppDetail(Long id);

    Flux<String> chatToGenCode(Long appId, String message, UserVO currentUser);

    String deployApp(Long appId, UserVO loginUser);
}

