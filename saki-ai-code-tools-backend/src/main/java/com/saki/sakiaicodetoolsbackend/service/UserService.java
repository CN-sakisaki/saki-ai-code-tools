package com.saki.sakiaicodetoolsbackend.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 用户表 服务层。
 *
 * @author saki酱
 * @since 2025-10-15
 */
public interface UserService extends IService<User> {
    /**
     * 登录。
     *
     * @param request 登录请求
     * @return 用户信息
     */
    UserVO login(LoginRequest request, HttpServletRequest httpServletRequest);

    /**
     * 用户注册。
     *
     * @param request 注册请求
     * @return 新用户的主键 ID
     */
    Long register(RegisterRequest request, HttpServletRequest httpServletRequest);

    /**
     * 管理员新增用户。
     *
     * @param request 新增用户请求
     * @return 新增用户的主键 ID
     */
    Long createUser(UserAddRequest request, HttpServletRequest httpServletRequest);

    /**
     * 管理员删除用户。
     *
     * @param request 删除请求
     * @return 删除是否成功
     */
    Boolean deleteUsers(UserDeleteRequest request, HttpServletRequest httpServletRequest);

    /**
     * 管理员更新用户信息。
     *
     * @param request 更新请求
     * @return 更新是否成功
     */
    Boolean updateUser(UserUpdateRequest request, HttpServletRequest httpServletRequest);

    /**
     * 分页查询用户列表。
     *
     * @param request 查询请求
     * @return 分页结果
     */
    Page<User> listUsersByPage(UserQueryRequest request, HttpServletRequest httpServletRequest);

    /**
     * 根据主键获取用户详情。
     *
     * @param id 用户ID
     * @return 用户详情
     */
    User getUserDetail(Long id, HttpServletRequest httpServletRequest);

    /**
     * 根据主键获取用户详情。
     *
     * @param id 用户ID
     * @return 用户VO详情
     */
    UserVO getUserVODetail(Long id, HttpServletRequest httpServletRequest);

    /**
     * 更新当前用户的基础资料。
     *
     * @param request 更新请求
     * @return 是否更新成功
     */
    Boolean updateCurrentUserProfile(UserProfileUpdateRequest request, HttpServletRequest httpServletRequest);

    /**
     * 更新当前用户的邮箱。
     *
     * @param request 更新请求
     * @return 是否更新成功
     */
    Boolean updateCurrentUserEmail(UserEmailUpdateRequest request, HttpServletRequest httpServletRequest);

    /**
     * 更新当前用户的手机号。
     *
     * @param request 更新请求
     * @return 是否更新成功
     */
    Boolean updateCurrentUserPhone(UserPhoneUpdateRequest request, HttpServletRequest httpServletRequest);


    /**
     * 退出登录，清理 Session。
     *
     * @param httpServletRequest 当前请求
     */
    void logout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse);


    /**
     * 获取验证码
     * @param request - 发送请求；
     * @return 是否发送成功
     */
    Boolean sendCode(SendCodeRequest request, HttpServletRequest httpServletRequest);

    /**
     * 获取当前登录用户
     * @param httpServletRequest HTTP请求对象
     * @return 脱敏后到用户信息
     */
    UserVO getCurrentUserInfo(HttpServletRequest httpServletRequest);
}
