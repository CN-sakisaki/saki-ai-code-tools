package com.saki.sakiaicodetoolsbackend.controller;

import com.mybatisflex.core.paginate.Page;
import com.saki.sakiaicodetoolsbackend.annotation.AuthCheck;
import com.saki.sakiaicodetoolsbackend.annotation.NoAuth;
import com.saki.sakiaicodetoolsbackend.common.BaseResponse;
import com.saki.sakiaicodetoolsbackend.common.ResultUtils;
import com.saki.sakiaicodetoolsbackend.constant.UserRoleConstant;
import com.saki.sakiaicodetoolsbackend.context.UserContext;
import com.saki.sakiaicodetoolsbackend.exception.ErrorCode;
import com.saki.sakiaicodetoolsbackend.exception.ThrowUtils;
import com.saki.sakiaicodetoolsbackend.model.dto.admin.user.UserAddRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.admin.user.UserDeleteRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.admin.user.UserQueryRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.admin.user.UserUpdateRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.login.LoginRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.login.RegisterRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.login.TokenRefreshRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.user.UserEmailGetCodeRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.user.UserEmailUpdateRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.user.UserPhoneUpdateRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.user.UserProfileUpdateRequest;
import com.saki.sakiaicodetoolsbackend.model.entity.User;
import com.saki.sakiaicodetoolsbackend.model.vo.UserVO;
import com.saki.sakiaicodetoolsbackend.service.UserService;
import com.saki.sakiaicodetoolsbackend.service.mail.MailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

/**
 * 用户表 控制层。
 *
 * @author saki酱
 * @since 2025-10-15
 */
@RestController
@Tag(name = "UserController", description = "用户表接口")
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @NoAuth
    @PostMapping("/register")
    @Operation(description = "用户注册")
    public BaseResponse<Long> register(@RequestBody RegisterRequest request) {
        return ResultUtils.success(userService.register(request));
    }

    @NoAuth
    @PostMapping("/login")
    @Operation(description = "用户登录")
    public BaseResponse<UserVO> login(@RequestBody LoginRequest request, HttpServletRequest httpServletRequest) {
        return ResultUtils.success(userService.login(request, httpServletRequest));
    }

    @NoAuth
    @PostMapping("/login/send-email-code")
    @Operation(description = "发送邮箱登录验证码")
    public BaseResponse<Boolean> sendEmailLoginCode(@RequestBody LoginRequest request) {
        userService.sendEmailLoginCode(request);
        return ResultUtils.success(Boolean.TRUE);
    }

    @NoAuth
    @PostMapping("/token/refresh")
    @Operation(description = "刷新 AccessToken")
    public BaseResponse<String> refreshAccessToken(@RequestBody TokenRefreshRequest request) {
        return ResultUtils.success(userService.refreshAccessToken(request));
    }

    /**
     * 获取当前登录的用户
     */
    @GetMapping("/get/info")
    @Operation(description = "获取当前登录用户")
    public BaseResponse<UserVO> getUserInfo() {
        User currentUser = UserContext.getUser();
        ThrowUtils.throwIf(currentUser == null, ErrorCode.NOT_FOUND_ERROR, "未登录或用户不存在");
        UserVO vo = new UserVO();
        vo.copyUserInfoFrom(currentUser);
        return ResultUtils.success(vo);
    }

    /**
     * 管理员新增用户。
     */
    @PostMapping("/admin/add")
    @AuthCheck(mustRole = UserRoleConstant.ADMIN_ROLE)
    @Operation(description = "管理员创建用户")
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest request) {
        return ResultUtils.success(userService.createUser(request));
    }

    /**
     * 管理员删除用户。
     */
    @PostMapping("/admin/delete")
    @AuthCheck(mustRole = UserRoleConstant.ADMIN_ROLE)
    @Operation(description = "管理员删除用户")
    public BaseResponse<Boolean> deleteUsers(@RequestBody UserDeleteRequest request) {
        return ResultUtils.success(userService.deleteUsers(request));
    }

    /**
     * 管理员更新用户信息。
     */
    @PutMapping("/admin/update")
    @AuthCheck(mustRole = UserRoleConstant.ADMIN_ROLE)
    @Operation(description = "管理员更新用户信息")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest request) {
        return ResultUtils.success(userService.updateUser(request));
    }

    /**
     * 管理员分页查询用户列表。
     */
    @PostMapping("/admin/list/page")
    @AuthCheck(mustRole = UserRoleConstant.ADMIN_ROLE)
    @Operation(description = "管理员分页获取用户列表")
    public BaseResponse<Page<User>> listUsersByPage(@RequestBody UserQueryRequest request) {
        return ResultUtils.success(userService.listUsersByPage(request));
    }

    /**
     * 管理员根据 ID 查询用户详情。
     */
    @GetMapping("/admin/{id}")
    @AuthCheck(mustRole = UserRoleConstant.ADMIN_ROLE)
    @Operation(description = "管理员根据 ID 获取用户详情")
    public BaseResponse<User> baseAdminGetUserById(@PathVariable Long id) {
        return ResultUtils.success(userService.getUserDetail(id));
    }

    /**
     * 用户根据 ID 查询用户详情。
     */
    @GetMapping("/{id}")
    @AuthCheck(mustRole = UserRoleConstant.USER_ROLE)
    @Operation(description = "用户根据 ID 获取用户详情")
    public BaseResponse<UserVO> baseUserGetUserById(@PathVariable Long id) {
        return ResultUtils.success(userService.getUserVODetail(id));
    }

    /**
     * 用户更新个人资料。
     */
    @PutMapping("/profile")
    @Operation(description = "更新个人资料")
    public BaseResponse<Boolean> updateProfile(@RequestBody UserProfileUpdateRequest request) {
        return ResultUtils.success(userService.updateCurrentUserProfile(request));
    }

    /**
     * 用户更新邮箱。
     */
    @PutMapping("/email")
    @Operation(description = "更新个人邮箱")
    public BaseResponse<Boolean> updateEmail(@RequestBody UserEmailUpdateRequest request) {
        return ResultUtils.success(userService.updateCurrentUserEmail(request));
    }

    /**
     * 用户更新手机号。
     */
    @PutMapping("/phone")
    @Operation(description = "更新个人手机号")
    public BaseResponse<Boolean> updatePhone(@RequestBody UserPhoneUpdateRequest request) {
        return ResultUtils.success(userService.updateCurrentUserPhone(request));
    }

    /**
     * 邮箱验证码发送接口。
     * <p>
     * 用户输入邮箱后，系统生成随机验证码，
     * 通过 {@link MailService} 发送邮件，
     * 并将验证码缓存至 Redis，设置有效期。
     * </p>
     * @param request 用户输入的邮箱地址
     * @return 通用响应结果
     */
    @PostMapping("/sendEmailCode")
    public BaseResponse<Boolean> sendEmailCode(@RequestBody UserEmailGetCodeRequest request) {
        return ResultUtils.success(userService.sendEmailCode(request));
    }

}
