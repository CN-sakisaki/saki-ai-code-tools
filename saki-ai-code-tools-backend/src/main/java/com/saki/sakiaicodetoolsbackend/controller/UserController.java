package com.saki.sakiaicodetoolsbackend.controller;

import com.mybatisflex.core.paginate.Page;
import com.saki.sakiaicodetoolsbackend.annotation.AuthCheck;
import com.saki.sakiaicodetoolsbackend.common.BaseResponse;
import com.saki.sakiaicodetoolsbackend.common.ResultUtils;
import com.saki.sakiaicodetoolsbackend.constant.UserRoleConstant;
import com.saki.sakiaicodetoolsbackend.exception.BusinessException;
import com.saki.sakiaicodetoolsbackend.model.dto.admin.user.UserAddRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.admin.user.UserDeleteRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.admin.user.UserQueryRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.admin.user.UserUpdateRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.common.SendCodeRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.login.LoginRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.login.RegisterRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.user.UserEmailUpdateRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.user.UserPhoneUpdateRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.user.UserProfileUpdateRequest;
import com.saki.sakiaicodetoolsbackend.model.entity.User;
import com.saki.sakiaicodetoolsbackend.model.vo.UserVO;
import com.saki.sakiaicodetoolsbackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户表 控制层
 * 提供用户注册、登录、信息管理等相关接口
 *
 * @author saki酱
 * @since 2025-10-15
 */
@RestController
@Tag(name = "UserController", description = "用户表接口")
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    /**
     * 用户服务
     */
    private final UserService userService;

    /**
     * 用户注册
     *
     * @param request 注册请求参数
     * @param httpServletRequest HTTP请求对象
     * @return 注册成功的用户ID
     */
    @PostMapping("/register")
    @Operation(description = "用户注册")
    public BaseResponse<Long> register(@RequestBody RegisterRequest request,
                                       HttpServletRequest httpServletRequest) {
        return ResultUtils.success(userService.register(request, httpServletRequest));
    }

    /**
     * 用户登录
     *
     * @param request 登录请求参数
     * @param httpServletRequest HTTP请求对象
     * @return 登录用户信息
     */
    @PostMapping("/login")
    @Operation(description = "用户登录")
    public BaseResponse<UserVO> login(@RequestBody LoginRequest request,
                                      HttpServletRequest httpServletRequest) {
        return ResultUtils.success(userService.login(request, httpServletRequest));
    }

    /**
     * 获取当前登录的用户信息
     *
     * @param httpServletRequest HTTP请求对象
     * @return 当前登录用户信息
     * @throws BusinessException 当用户未登录或会话失效时抛出
     */
    @GetMapping("/get/info")
    @Operation(description = "获取当前登录用户")
    public BaseResponse<UserVO> getUserInfo(HttpServletRequest httpServletRequest) {
        return ResultUtils.success(userService.getCurrentUserInfo(httpServletRequest));
    }

    /**
     * 管理员新增用户
     *
     * @param request 用户添加请求参数
     * @param httpServletRequest HTTP请求对象
     * @return 新增用户的ID
     */
    @PostMapping("/admin/add")
    @AuthCheck(mustRole = UserRoleConstant.ADMIN_ROLE)
    @Operation(description = "管理员创建用户")
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest request,
                                      HttpServletRequest httpServletRequest) {
        return ResultUtils.success(userService.createUser(request, httpServletRequest));
    }

    /**
     * 管理员删除用户
     *
     * @param request 用户删除请求参数
     * @param httpServletRequest HTTP请求对象
     * @return 删除是否成功
     */
    @PostMapping("/admin/delete")
    @AuthCheck(mustRole = UserRoleConstant.ADMIN_ROLE)
    @Operation(description = "管理员删除用户")
    public BaseResponse<Boolean> deleteUsers(@RequestBody UserDeleteRequest request,
                                             HttpServletRequest httpServletRequest) {
        return ResultUtils.success(userService.deleteUsers(request, httpServletRequest));
    }

    /**
     * 管理员更新用户信息
     *
     * @param request 用户更新请求参数
     * @param httpServletRequest HTTP请求对象
     * @return 更新是否成功
     */
    @PutMapping("/admin/update")
    @AuthCheck(mustRole = UserRoleConstant.ADMIN_ROLE)
    @Operation(description = "管理员更新用户信息")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest request,
                                            HttpServletRequest httpServletRequest) {
        return ResultUtils.success(userService.updateUser(request, httpServletRequest));
    }

    /**
     * 管理员分页查询用户列表
     *
     * @param request 用户查询请求参数
     * @param httpServletRequest HTTP请求对象
     * @return 分页用户列表
     */
    @PostMapping("/admin/list/page")
    @AuthCheck(mustRole = UserRoleConstant.ADMIN_ROLE)
    @Operation(description = "管理员分页获取用户列表")
    public BaseResponse<Page<User>> listUsersByPage(@RequestBody UserQueryRequest request,
                                                    HttpServletRequest httpServletRequest) {
        return ResultUtils.success(userService.listUsersByPage(request, httpServletRequest));
    }

    /**
     * 管理员根据 ID 查询用户详情
     *
     * @param id 用户ID
     * @param httpServletRequest HTTP请求对象
     * @return 用户详细信息
     */
    @GetMapping("/admin/{id}")
    @AuthCheck(mustRole = UserRoleConstant.ADMIN_ROLE)
    @Operation(description = "管理员根据 ID 获取用户详情")
    public BaseResponse<User> baseAdminGetUserById(@PathVariable Long id,
                                                   HttpServletRequest httpServletRequest) {
        return ResultUtils.success(userService.getUserDetail(id, httpServletRequest));
    }

    /**
     * 用户根据 ID 查询用户详情
     *
     * @param id 用户ID
     * @param httpServletRequest HTTP请求对象
     * @return 用户VO信息
     */
    @GetMapping("/{id}")
    @AuthCheck(mustRole = UserRoleConstant.USER_ROLE)
    @Operation(description = "用户根据 ID 获取用户详情")
    public BaseResponse<UserVO> baseUserGetUserById(@PathVariable Long id,
                                                    HttpServletRequest httpServletRequest) {
        return ResultUtils.success(userService.getUserVODetail(id, httpServletRequest));
    }

    /**
     * 用户更新个人资料
     *
     * @param request 用户资料更新请求
     * @param httpServletRequest HTTP请求对象
     * @return 更新是否成功
     */
    @PutMapping("/profile")
    @Operation(description = "更新个人资料")
    public BaseResponse<Boolean> updateProfile(@RequestBody UserProfileUpdateRequest request,
                                               HttpServletRequest httpServletRequest) {
        return ResultUtils.success(userService.updateCurrentUserProfile(request, httpServletRequest));
    }

    /**
     * 用户更新邮箱
     *
     * @param request 邮箱更新请求
     * @param httpServletRequest HTTP请求对象
     * @return 更新是否成功
     */
    @PutMapping("/email")
    @Operation(description = "更新个人邮箱")
    public BaseResponse<Boolean> updateEmail(@RequestBody UserEmailUpdateRequest request,
                                             HttpServletRequest httpServletRequest) {
        return ResultUtils.success(userService.updateCurrentUserEmail(request, httpServletRequest));
    }

    /**
     * 用户更新手机号
     *
     * @param request 手机号更新请求
     * @param httpServletRequest HTTP请求对象
     * @return 更新是否成功
     */
    @PutMapping("/phone")
    @Operation(description = "更新个人手机号")
    public BaseResponse<Boolean> updatePhone(@RequestBody UserPhoneUpdateRequest request,
                                             HttpServletRequest httpServletRequest) {
        return ResultUtils.success(userService.updateCurrentUserPhone(request, httpServletRequest));
    }

    /**
     * 邮箱验证码发送接口
     * <p>
     * 用户输入邮箱后，系统生成随机验证码并发送邮件，
     * 同时将验证码缓存至 Redis，设置有效期。
     * </p>
     *
     * @param request 验证码发送请求参数
     * @param httpServletRequest HTTP请求对象
     * @return 发送是否成功
     */
    @PostMapping("/sendCode")
    @Operation(description = "邮箱验证码发送")
    public BaseResponse<Boolean> sendCode(@RequestBody SendCodeRequest request,
                                          HttpServletRequest httpServletRequest) {
        return ResultUtils.success(userService.sendCode(request, httpServletRequest));
    }

    /**
     * 用户退出登录
     *
     * @param httpServletRequest HTTP请求对象
     * @param httpServletResponse HTTP响应对象
     * @return 退出是否成功
     */
    @PostMapping("/logout")
    @Operation(description = "退出登录")
    public BaseResponse<Boolean> logout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        userService.logout(httpServletRequest, httpServletResponse);
        return ResultUtils.success(Boolean.TRUE);
    }
}