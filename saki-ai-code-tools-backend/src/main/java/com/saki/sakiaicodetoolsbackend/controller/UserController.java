package com.saki.sakiaicodetoolsbackend.controller;

import com.mybatisflex.core.paginate.Page;
import com.saki.sakiaicodetoolsbackend.common.BaseResponse;
import com.saki.sakiaicodetoolsbackend.common.ResultUtils;
import com.saki.sakiaicodetoolsbackend.context.UserContext;
import com.saki.sakiaicodetoolsbackend.exception.ErrorCode;
import com.saki.sakiaicodetoolsbackend.exception.ThrowUtils;
import com.saki.sakiaicodetoolsbackend.model.dto.login.LoginRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.login.RegisterRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.login.TokenRefreshRequest;
import com.saki.sakiaicodetoolsbackend.model.entity.User;
import com.saki.sakiaicodetoolsbackend.model.vo.UserVO;
import com.saki.sakiaicodetoolsbackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户表 控制层。
 *
 * @author saki酱
 * @since 2025-10-15
 */
@RestController
@Tag(name = "用户表接口")
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    @Operation(description = "用户注册")
    public BaseResponse<Long> register(@RequestBody RegisterRequest request) {
        return ResultUtils.success(userService.register(request));
    }

    @PostMapping("/login")
    @Operation(description = "用户登录")
    public BaseResponse<UserVO> login(@RequestBody LoginRequest request, HttpServletRequest httpServletRequest) {
        return ResultUtils.success(userService.login(request, httpServletRequest));
    }

    @PostMapping("/login/send-email-code")
    @Operation(description = "发送邮箱登录验证码")
    public BaseResponse<Boolean> sendEmailLoginCode(@RequestBody LoginRequest request) {
        userService.sendEmailLoginCode(request);
        return ResultUtils.success(Boolean.TRUE);
    }

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
        UserVO vo = new UserVO();
        vo.copyUserInfoFrom(currentUser);
        ThrowUtils.throwIf(currentUser == null, ErrorCode.NOT_FOUND_ERROR, "未登录或用户不存在");
        return ResultUtils.success(vo);
    }

    /**
     * 保存用户表。
     *
     * @param user 用户表
     * @return {@code true} 保存成功，{@code false} 保存失败
     */
    @PostMapping("save")
    @Operation(description="保存用户表")
    public boolean save(@RequestBody @Parameter(description="用户表")User user) {
        return userService.save(user);
    }

    /**
     * 根据主键删除用户表。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove/{id}")
    @Operation(description="根据主键删除用户表")
    public boolean remove(@PathVariable @Parameter(description="用户表主键") Long id) {
        return userService.removeById(id);
    }

    /**
     * 根据主键更新用户表。
     *
     * @param user 用户表
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("update")
    @Operation(description="根据主键更新用户表")
    public boolean update(@RequestBody @Parameter(description="用户表主键") User user) {
        return userService.updateById(user);
    }

    /**
     * 查询所有用户表。
     *
     * @return 所有数据
     */
    @GetMapping("list")
    @Operation(description="查询所有用户表")
    public List<User> list() {
        return userService.list();
    }

    /**
     * 根据主键获取用户表。
     *
     * @param id 用户表主键
     * @return 用户表详情
     */
    @GetMapping("getInfo/{id}")
    @Operation(description="根据主键获取用户表")
    public User getInfo(@PathVariable @Parameter(description="用户表主键") Long id) {
        return userService.getById(id);
    }

    /**
     * 分页查询用户表。
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("page")
    @Operation(description="分页查询用户表")
    public Page<User> page(@Parameter(description="分页信息") Page<User> page) {
        return userService.page(page);
    }

}
