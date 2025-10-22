package com.saki.sakiaicodetoolsbackend.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.saki.sakiaicodetoolsbackend.model.dto.admin.user.UserAddRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.admin.user.UserDeleteRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.admin.user.UserQueryRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.admin.user.UserUpdateRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.login.LoginRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.login.RegisterRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.user.UserEmailGetCodeRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.user.UserEmailUpdateRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.user.UserPhoneUpdateRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.user.UserProfileUpdateRequest;
import com.saki.sakiaicodetoolsbackend.model.entity.User;
import com.saki.sakiaicodetoolsbackend.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

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
     * 发送邮箱登录验证码。
     *
     * @param request 登录请求
     */
    void sendEmailLoginCode(LoginRequest request, HttpServletRequest httpServletRequest);

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
     * 根据邮箱获取验证吗。
     *
     * @param request 获取请求
     * @return 是否获取成功
     */
    Boolean sendEmailCode(UserEmailGetCodeRequest request, HttpServletRequest httpServletRequest);

    /**
     * 上传用户头像并返回访问地址。
     *
     * @param file          待上传的头像文件
     * @param targetUserId  目标用户ID，若为空则默认取当前登录用户
     * @return 头像在对象存储中的访问地址
     */
    String uploadAvatar(MultipartFile file, Long targetUserId, HttpServletRequest httpServletRequest);

    /**
     * 退出登录，清理 Session。
     *
     * @param httpServletRequest 当前请求
     */
    void logout(HttpServletRequest httpServletRequest);
}
