package com.saki.sakiaicodetoolsbackend.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.mybatisflex.core.paginate.Page;
import com.saki.sakiaicodetoolsbackend.annotation.AuthCheck;
import com.saki.sakiaicodetoolsbackend.common.BaseResponse;
import com.saki.sakiaicodetoolsbackend.common.ResultUtils;
import com.saki.sakiaicodetoolsbackend.constant.UserRoleConstant;
import com.saki.sakiaicodetoolsbackend.exception.ErrorCode;
import com.saki.sakiaicodetoolsbackend.exception.ThrowUtils;
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
import com.saki.sakiaicodetoolsbackend.service.AppService;
import com.saki.sakiaicodetoolsbackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 应用模块接口。
 *
 * <p>提供用户和管理员的应用管理能力，包括创建、更新、删除和查询等操作。</p>
 *
 * @author saki酱
 * @since 2025-10-21
 */
@RestController
@RequestMapping("/app")
@Tag(name = "AppController", description = "应用管理接口")
@RequiredArgsConstructor
public class AppController {

    private final AppService appService;
    private final UserService userService;

    /**
     * 用户创建应用。
     *
     * @param request             创建请求
     * @param httpServletRequest  HTTP 请求
     * @return 新应用 ID
     */
    @PostMapping
    @AuthCheck(mustRole = UserRoleConstant.USER_ROLE)
    @Operation(summary = "用户创建应用", description = "普通用户创建自己的应用，必须填写初始化 Prompt")
    public BaseResponse<Long> createApp(@RequestBody AppCreateRequest request,
                                        HttpServletRequest httpServletRequest) {
        return ResultUtils.success(appService.createApp(request, httpServletRequest));
    }

    /**
     * 用户更新自己的应用名称。
     *
     * @param id                  应用主键
     * @param request             更新请求
     * @param httpServletRequest  HTTP 请求
     * @return 是否更新成功
     */
    @PutMapping("/{id}")
    @AuthCheck(mustRole = UserRoleConstant.USER_ROLE)
    @Operation(summary = "用户更新应用", description = "普通用户根据 ID 更新自己的应用名称")
    public BaseResponse<Boolean> updateMyApp(@PathVariable Long id,
                                             @RequestBody AppUpdateRequest request,
                                             HttpServletRequest httpServletRequest) {
        request.setId(id);
        return ResultUtils.success(appService.updateMyApp(request, httpServletRequest));
    }

    /**
     * 用户删除自己的应用。
     *
     * @param id                  应用主键
     * @param httpServletRequest  HTTP 请求
     * @return 是否删除成功
     */
    @DeleteMapping("/{id}")
    @AuthCheck(mustRole = UserRoleConstant.USER_ROLE)
    @Operation(summary = "用户删除应用", description = "普通用户根据 ID 删除自己的应用")
    public BaseResponse<Boolean> deleteMyApp(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        return ResultUtils.success(appService.deleteMyApp(id, httpServletRequest));
    }

    /**
     * 用户查看自己的应用详情。
     *
     * @param id                  应用主键
     * @param httpServletRequest  HTTP 请求
     * @return 应用详情
     */
    @GetMapping("/{id}")
    @AuthCheck(mustRole = UserRoleConstant.USER_ROLE)
    @Operation(summary = "用户查看应用详情", description = "普通用户根据 ID 查看自己应用的详情")
    public BaseResponse<AppVO> getMyApp(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        return ResultUtils.success(appService.getMyAppDetail(id, httpServletRequest));
    }

    /**
     * 用户分页查询自己的应用列表。
     *
     * @param request             查询请求
     * @param httpServletRequest  HTTP 请求
     * @return 应用分页结果
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserRoleConstant.USER_ROLE)
    @Operation(summary = "用户分页查询应用", description = "普通用户分页获取自己的应用列表，支持名称模糊查询")
    public BaseResponse<Page<AppVO>> listMyApps(@RequestBody AppMyListQueryRequest request,
                                                HttpServletRequest httpServletRequest) {
        return ResultUtils.success(appService.listMyAppsByPage(request, httpServletRequest));
    }

    /**
     * 分页查询精选应用列表。
     *
     * @param request 查询请求
     * @return 精选应用分页结果
     */
    @PostMapping("/featured/list/page")
    @Operation(summary = "查询精选应用", description = "公开接口，分页查询精选应用列表，支持名称模糊查询")
    public BaseResponse<Page<AppVO>> listFeaturedApps(@RequestBody AppFeaturedListQueryRequest request) {
        return ResultUtils.success(appService.listFeaturedAppsByPage(request));
    }

    /**
     * 管理员批量删除应用。
     *
     * @param request 删除请求
     * @return 是否删除成功
     */
    @PostMapping("/admin/delete")
    @AuthCheck(mustRole = UserRoleConstant.ADMIN_ROLE)
    @Operation(summary = "管理员删除应用", description = "管理员根据 ID 列表批量删除应用")
    public BaseResponse<Boolean> adminDeleteApps(@RequestBody AppAdminDeleteRequest request) {
        return ResultUtils.success(appService.adminDeleteApps(request));
    }

    /**
     * 管理员更新应用信息。
     *
     * @param id      应用主键
     * @param request 更新请求
     * @return 是否更新成功
     */
    @PutMapping("/admin/{id}")
    @AuthCheck(mustRole = UserRoleConstant.ADMIN_ROLE)
    @Operation(summary = "管理员更新应用", description = "管理员根据 ID 更新应用的名称、封面和优先级")
    public BaseResponse<Boolean> adminUpdateApp(@PathVariable Long id,
                                                @RequestBody AppAdminUpdateRequest request) {
        request.setId(id);
        return ResultUtils.success(appService.adminUpdateApp(request));
    }

    /**
     * 管理员分页查询应用列表。
     *
     * @param request 查询请求
     * @return 应用分页结果
     */
    @PostMapping("/admin/list/page")
    @AuthCheck(mustRole = UserRoleConstant.ADMIN_ROLE)
    @Operation(summary = "管理员分页查询应用", description = "管理员分页查询应用列表，支持除时间字段外的多条件查询")
    public BaseResponse<Page<App>> adminListApps(@RequestBody AppAdminQueryRequest request) {
        return ResultUtils.success(appService.adminListAppsByPage(request));
    }

    /**
     * 管理员查看应用详情。
     *
     * @param id 应用主键
     * @return 应用详情
     */
    @GetMapping("/admin/{id}")
    @AuthCheck(mustRole = UserRoleConstant.ADMIN_ROLE)
    @Operation(summary = "管理员查看应用详情", description = "管理员根据 ID 查看应用详情")
    public BaseResponse<App> adminGetAppDetail(@PathVariable Long id) {
        return ResultUtils.success(appService.getAdminAppDetail(id));
    }

    /**
     * 应用聊天生成代码（流式 SSE）
     *
     * @param appId   应用 ID
     * @param message 用户消息
     * @param request 请求对象
     * @return 生成结果流
     */
    @GetMapping(value = "/chat/gen/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Object>> chatToGenCode(@RequestParam Long appId,
                                                       @RequestParam String message,
                                                       HttpServletRequest request) {
        // 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        // 获取当前登录用户
        UserVO currentUser = userService.getCurrentUserInfo(request);
        // 调用服务生成代码（流式）
        // return appService.chatToGenCode(appId, message, currentUser);
        Flux<String> contentFlux = appService.chatToGenCode(appId, message, currentUser);
        return contentFlux
                .map(chunk -> {
                    Map<String, String> wrapper = Map.of("d", chunk);
                    String jsonData = JSONUtil.toJsonStr((wrapper));
                    return ServerSentEvent
                            .builder()
                            .data(jsonData)
                            .build();
                })
                .concatWith(Mono.just(
                        ServerSentEvent
                                .builder()
                                .event("done")
                                .data("")
                                .build()));
    }
}

