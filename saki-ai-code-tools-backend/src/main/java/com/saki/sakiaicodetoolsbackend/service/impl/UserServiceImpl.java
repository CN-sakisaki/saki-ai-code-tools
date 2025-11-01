package com.saki.sakiaicodetoolsbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.saki.sakiaicodetoolsbackend.constant.AuthConstants;
import com.saki.sakiaicodetoolsbackend.constant.UserConstants;
import com.saki.sakiaicodetoolsbackend.constant.UserFieldConstants;
import com.saki.sakiaicodetoolsbackend.exception.BusinessException;
import com.saki.sakiaicodetoolsbackend.exception.ErrorCode;
import com.saki.sakiaicodetoolsbackend.exception.ThrowUtils;
import com.saki.sakiaicodetoolsbackend.mapper.UserMapper;
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
import com.saki.sakiaicodetoolsbackend.model.enums.LoginTypeEnum;
import com.saki.sakiaicodetoolsbackend.model.enums.VerificationSceneEnum;
import com.saki.sakiaicodetoolsbackend.model.enums.VipStatusEnum;
import com.saki.sakiaicodetoolsbackend.model.vo.UserVO;
import com.saki.sakiaicodetoolsbackend.service.UserService;
import com.saki.sakiaicodetoolsbackend.service.email.strategy.scene.VerificationSceneFactory;
import com.saki.sakiaicodetoolsbackend.service.login.LoginStrategyFactory;
import com.saki.sakiaicodetoolsbackend.utils.ChannelDetectorUtils;
import com.saki.sakiaicodetoolsbackend.utils.IpUtils;
import com.saki.sakiaicodetoolsbackend.utils.QueryWrapperUtils;
import com.saki.sakiaicodetoolsbackend.utils.VipTimeUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 * 负责用户登录、注册、信息管理、权限控制等核心业务逻辑
 * 继承MyBatis-Flex的ServiceImpl，提供基础的CRUD操作，并实现自定义的用户服务接口
 *
 * @author saki酱
 * @version 1.0
 * @since 2025-10-17 11:53
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /** 升序排序标识 */
    private static final String SORT_ORDER_ASC = "ascend";
    /** 默认排序字段 */
    private static final String DEFAULT_SORT_COLUMN = "create_time";

    /** 排序字段映射表 - 前端字段名到数据库字段名的映射 */
    private static final Map<String, String> SORT_FIELD_MAP = Map.ofEntries(
            Map.entry("id", UserFieldConstants.ID),
            Map.entry("createTime", UserFieldConstants.CREATE_TIME),
            Map.entry("updateTime", UserFieldConstants.UPDATE_TIME),
            Map.entry("lastLoginTime", UserFieldConstants.LAST_LOGIN_TIME),
            Map.entry("userAccount", UserFieldConstants.USER_ACCOUNT),
            Map.entry("userName", UserFieldConstants.USER_NAME),
            Map.entry("userRole", UserFieldConstants.USER_ROLE),
            Map.entry("userStatus", UserFieldConstants.USER_STATUS),
            Map.entry("isVip", UserFieldConstants.IS_VIP)
    );

    /** 登录策略工厂，用于根据不同登录类型选择认证策略 */
    private final LoginStrategyFactory loginStrategyFactory;

    /** Redis模板，用于存储刷新令牌和验证码 */
    private final StringRedisTemplate stringRedisTemplate;

    /** 用户数据访问接口 */
    private final UserMapper userMapper;

    /** 验证场景工厂，用于处理不同业务场景的验证码发送逻辑 */
    private final VerificationSceneFactory sceneFactory;

    // ===================== 登录相关方法 =====================

    /**
     * 用户登录方法，支持多种登录方式
     * 根据登录类型选择相应的认证策略，认证成功后生成令牌并更新登录信息
     *
     * @param request 登录请求对象，包含登录类型、凭证等信息
     * @param httpServletRequest HTTP请求对象
     * @return 用户视图对象，包含用户基本信息和访问令牌
     * @throws BusinessException 当请求参数为空、登录类型不支持或认证失败时抛出
     */
    @Override
    public UserVO login(LoginRequest request, HttpServletRequest httpServletRequest) {
        // 验证请求参数
        validateRequest(request, "请求参数不能为空");

        // 解析登录类型，如果不存在则抛出异常
        LoginTypeEnum loginType = LoginTypeEnum.fromValue(request.getLoginType())
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的登录方式"));

        // 使用策略模式进行用户认证 - 根据登录类型选择对应的认证策略
        User user = loginStrategyFactory.getStrategy(loginType).authenticate(request);

        // 构建登录结果
        return buildLoginResult(user, httpServletRequest);
    }

    /**
     * 用户注册
     * 包含账号密码校验、重复性检查、密码加密和用户信息初始化
     *
     * @param request 注册请求对象
     * @param httpServletRequest HTTP请求对象
     * @return 新用户的主键 ID
     * @throws BusinessException 当参数校验失败、账号已存在或保存失败时抛出
     */
    @Override
    public Long register(RegisterRequest request, HttpServletRequest httpServletRequest) {
        validateRequest(request, "注册请求不能为空");

        String userAccount = StrUtil.trim(request.getUserAccount());
        String password = request.getUserPassword();
        String confirmPassword = request.getConfirmPassword();
        String userEmail = request.getUserEmail();
        String code = request.getCode();

        // 基本参数校验 - 账号密码不能为空且长度至少8位
        ThrowUtils.throwIf(StrUtil.isBlank(userAccount) || StrUtil.isBlank(password) || StrUtil.isBlank(confirmPassword), ErrorCode.PARAMS_MISSING, "账号或密码不能为空");
        ThrowUtils.throwIf(!StrUtil.equals(password, confirmPassword), ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        ThrowUtils.throwIf(!StrUtil.equals(password, confirmPassword), ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        ThrowUtils.throwIf(userAccount.length() < 8 || password.length() < 8, ErrorCode.PARAMS_ERROR, "账号或密码长度至少8位");

        // 校验账号/邮箱是否已存在 - 使用QueryWrapper构建查询条件
        boolean accountExists = getOne(new QueryWrapper().where(User::getUserAccount).eq(userAccount).limit(1)) != null;
        ThrowUtils.throwIf(accountExists, ErrorCode.PARAMS_ERROR, "账号已存在");
        validateEmailAndPhone(userEmail, null, null);

        // 消费验证码
        String redisKey = AuthConstants.buildEmailCodeKey(VerificationSceneEnum.REGISTER, userEmail);
        validateAndConsumeCode(redisKey, code, "邮箱验证码已过期");

        // 生成密码盐值并进行加密 - 增强密码安全性
        String salt = generateUserSalt();
        String encryptedPassword = DigestUtil.sha256Hex(password + salt);

        // 生成唯一邀请码，如果请求中未提供则自动生成
        String inviteCode = Optional.ofNullable(StrUtil.trimToNull(request.getInviteCode())).orElseGet(this::generateUniqueInviteCode);

        // 构建新用户对象
        User newUser = User.builder()
                .userAccount(userAccount)
                .userEmail(userEmail)
                .userPassword(encryptedPassword)
                .userName(buildDefaultUserName(userAccount))
                .userRole(UserConstants.DEFAULT_USER_ROLE)
                .userStatus(UserConstants.DEFAULT_USER_STATUS)
                .isVip(UserConstants.DEFAULT_VIP_STATUS)
                .inviteCode(inviteCode)
                .userSalt(salt)
                .build();

        // 保存用户到数据库
        boolean saved = save(newUser);
        ThrowUtils.throwIf(!saved, ErrorCode.SYSTEM_ERROR, "用户注册失败");
        return newUser.getId();
    }

    // ===================== 管理员相关方法 =====================

    /**
     * 新增用户
     * <p>
     * 该方法会对请求参数进行严格校验，包括：
     * <ul>
     *   <li>账号与密码的非空与长度校验（均需≥8位）；</li>
     *   <li>邮箱与手机号的格式及唯一性校验；</li>
     *   <li>自动生成盐值并进行密码加密；</li>
     *   <li>为用户生成唯一邀请码及默认信息；</li>
     *   <li>若设置为 VIP 用户，则计算并设置会员起止时间。</li>
     * </ul>
     * 方法执行过程中使用 {@code @Transactional(rollbackFor = Exception.class)}，
     * 若任意异常发生将回滚事务，确保数据一致性。
     * </p>
     *
     * @param request 用户新增请求对象，不能为空。包含账号、密码、邮箱、手机号、昵称等用户基础信息。
     * @param httpServletRequest HTTP请求对象
     * @return 新增用户的主键 ID
     * @throws BusinessException 当请求参数不合法、数据重复或保存失败时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(UserAddRequest request, HttpServletRequest httpServletRequest) {
        validateRequest(request, "新增用户请求不能为空");

        // 账号与密码的非空与长度校验（均需≥8位）
        String userAccount = StrUtil.trimToNull(request.getUserAccount());
        String rawPassword = StrUtil.trimToNull(request.getUserPassword());
        ThrowUtils.throwIf(StrUtil.isBlank(userAccount), ErrorCode.PARAMS_MISSING, "用户账号不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(rawPassword), ErrorCode.PARAMS_MISSING, "用户密码不能为空");
        ThrowUtils.throwIf(userAccount.length() < 8 || rawPassword.length() < 8, ErrorCode.PARAMS_ERROR, "账号和密码长度至少8位");

        // 检查账号是否已存在
        ThrowUtils.throwIf(existsByColumn(UserFieldConstants.USER_ACCOUNT, userAccount, null), ErrorCode.DATA_ALREADY_EXISTS, "账号已存在");

        // 邮箱与手机号的格式及唯一性校验
        String email = StrUtil.trimToNull(request.getUserEmail());
        String phone = StrUtil.trimToNull(request.getUserPhone());
        validateEmailAndPhone(email, phone, null);

        // 自动生成盐值并进行密码加密
        String salt = generateUserSalt();
        String encryptedPassword = DigestUtil.sha256Hex(rawPassword + salt);

        // 为用户生成唯一邀请码
        String inviteCode = generateUniqueInviteCode();

        // 构建用户对象并复制属性
        User user = new User();
        BeanUtil.copyProperties(request, user, CopyOptions.create().ignoreNullValue().ignoreError());
        user.setUserPassword(encryptedPassword);
        user.setUserSalt(salt);
        user.setInviteCode(inviteCode);
        user.setUserName(StrUtil.blankToDefault(user.getUserName(), buildDefaultUserName(userAccount)));
        user.setUserRole(StrUtil.blankToDefault(user.getUserRole(), UserConstants.DEFAULT_USER_ROLE));
        user.setUserStatus(Optional.ofNullable(user.getUserStatus()).orElse(UserConstants.DEFAULT_USER_STATUS));

        // 处理VIP状态
        Integer isVip = Optional.ofNullable(user.getIsVip()).orElse(UserConstants.DEFAULT_VIP_STATUS);
        user.setIsVip(isVip);
        applyVipTimeIfNeeded(isVip, user);

        // 保存用户到数据库
        boolean saved = save(user);
        ThrowUtils.throwIf(!saved, ErrorCode.DATA_SAVE_FAILED, "新增用户失败");
        return user.getId();
    }

    /**
     * 批量删除用户
     * <p>
     * 该方法会根据传入的用户 ID 列表删除对应的用户记录，执行前会：
     * <ul>
     *   <li>验证请求参数是否合法（包括空值与 ID 列表非空校验）；</li>
     *   <li>查询数据库中实际存在的用户，校验待删除的 ID 是否全部存在；</li>
     *   <li>若存在不存在的用户 ID，将抛出异常提示；</li>
     *   <li>所有校验通过后，执行批量删除操作。</li>
     * </ul>
     * 方法使用 {@code @Transactional(rollbackFor = Exception.class)}，
     * 当出现任意异常时将自动回滚事务，确保数据一致性与安全性。
     * </p>
     *
     * @param request 用户删除请求对象，不能为空。内部应包含待删除的用户 ID 列表。
     * @param httpServletRequest HTTP请求对象
     * @return 删除结果标识，始终返回 {@code true} 表示删除成功
     * @throws BusinessException 当请求参数缺失、部分用户不存在或删除失败时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteUsers(UserDeleteRequest request, HttpServletRequest httpServletRequest) {
        validateRequest(request, "删除用户请求不能为空");
        List<Long> ids = request.getIds();
        ThrowUtils.throwIf(CollUtil.isEmpty(ids), ErrorCode.PARAMS_MISSING, "待删除的用户ID不能为空");

        // 一次性从数据库中查询出所有匹配这些 ID 的用户
        List<User> users = listByIds(ids);
        // 提取数据库中实际存在的用户 ID 集合
        Set<Long> existingIds = users.stream()
                .map(User::getId)
                .collect(Collectors.toSet());
        // 从前端传入的 ID 列表中过滤出不存在于数据库的 ID
        List<Long> missingIds = ids.stream()
                .filter(id -> !existingIds.contains(id))
                .collect(Collectors.toList());
        // 若存在未找到的用户 ID，则抛出异常并终止删除操作
        ThrowUtils.throwIf(CollUtil.isNotEmpty(missingIds), ErrorCode.NOT_FOUND_ERROR,
                "部分用户不存在，ID=" + missingIds.stream().map(String::valueOf).collect(Collectors.joining(",")));

        // 执行批量删除
        boolean removed = removeByIds(ids);
        ThrowUtils.throwIf(!removed, ErrorCode.DATA_DELETE_FAILED, "删除用户失败");
        return Boolean.TRUE;
    }

    /**
     * 更新用户信息
     * <p>
     * 该方法用于根据前端请求更新用户基本资料，包括账号、邮箱、手机号、角色、VIP 状态等信息。
     * 在执行更新前，会进行以下操作：
     * <ul>
     *     <li>验证请求参数合法性（包括用户 ID 非空校验）；</li>
     *     <li>查询数据库中原始用户信息，确保目标用户存在；</li>
     *     <li>校验邮箱和手机号的格式与唯一性（排除当前用户自身）；</li>
     *     <li>使用 {@link BeanUtil#copyProperties(Object, Object, CopyOptions)} 仅复制非空字段，避免覆盖数据库原有值；</li>
     *     <li>若用户为 VIP，则自动更新会员起止时间；</li>
     *     <li>最终调用 {@code updateById()} 执行持久化更新。</li>
     * </ul>
     * 方法带有 {@code @Transactional(rollbackFor = Exception.class)}，
     * 若执行过程中出现任何异常，将自动回滚事务，保证数据一致性。
     * </p>
     *
     * @param request 用户更新请求对象，不能为空，必须包含用户 ID 及待更新字段。
     * @param httpServletRequest HTTP请求对象
     * @return 更新结果标识，始终返回 {@code true} 表示更新成功
     * @throws BusinessException 当请求参数缺失、用户不存在、数据冲突或数据库操作失败时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateUser(UserUpdateRequest request, HttpServletRequest httpServletRequest) {
        // 参数非空校验
        validateRequest(request, "更新用户请求不能为空");
        Long id = request.getId();
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_MISSING, "用户ID不能为空");

        // 查询数据库中的原始用户信息
        User existingUser = getById(id);
        ThrowUtils.throwIf(existingUser == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");

        // 校验邮箱和手机号的格式与唯一性（排除当前用户）
        String newEmail = StrUtil.trim(request.getUserEmail());
        String newPhone = StrUtil.trim(request.getUserPhone());
        validateEmailAndPhone(newEmail, newPhone, id);

        // 将请求中非空字段复制到现有用户对象，避免覆盖原值为 null
        BeanUtil.copyProperties(request, existingUser, CopyOptions.create().ignoreNullValue().ignoreError());

        // 执行数据库更新操作
        boolean updated = updateById(existingUser);
        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "更新用户失败");
        return Boolean.TRUE;
    }

    /**
     * 分页查询用户列表
     * 支持多条件查询和动态排序，主要用于管理员查看用户列表
     *
     * @param request 用户查询请求参数
     * @param httpServletRequest HTTP请求对象
     * @return 分页用户列表
     * @throws BusinessException 当请求参数为空时抛出
     */
    @Override
    public Page<User> listUsersByPage(UserQueryRequest request, HttpServletRequest httpServletRequest) {
        validateRequest(request, "分页查询请求不能为空");

        // 创建分页对象
        Page<User> page = new Page<>(request.getPageNum(), request.getPageSize());
        // 构建基础查询条件 - 只查询未删除的用户
        QueryWrapper queryWrapper = QueryWrapper.create().where(User::getIsDelete).eq(0);

        // 动态添加查询条件 - 使用工具类避免空值判断
        QueryWrapperUtils.applyIfNotBlank(request.getUserAccount(), val -> queryWrapper.and(User::getUserAccount).like(val));
        QueryWrapperUtils.applyIfNotBlank(request.getUserName(), val -> queryWrapper.and(User::getUserName).like(val));
        QueryWrapperUtils.applyIfNotBlank(request.getUserEmail(), val -> queryWrapper.and(User::getUserEmail).like(val));
        QueryWrapperUtils.applyIfNotBlank(request.getUserPhone(), val -> queryWrapper.and(User::getUserPhone).like(val));
        QueryWrapperUtils.applyIfNotBlank(request.getUserRole(), val -> queryWrapper.and(User::getUserRole).eq(val));

        // 数值类型条件处理
        QueryWrapperUtils.applyIfNotNull(request.getUserStatus(), val -> queryWrapper.and(User::getUserStatus).eq(val));
        QueryWrapperUtils.applyIfNotNull(request.getIsVip(), val -> queryWrapper.and(User::getIsVip).eq(val));
        QueryWrapperUtils.applyIfNotNull(request.getVipStartTime(), val -> queryWrapper.and(User::getVipStartTime).ge(val));
        QueryWrapperUtils.applyIfNotNull(request.getVipEndTime(), val -> queryWrapper.and(User::getVipEndTime).le(val));
        QueryWrapperUtils.applyIfNotNull(request.getLastLoginTime(), val -> queryWrapper.and(User::getLastLoginTime).ge(val));
        QueryWrapperUtils.applyIfNotNull(request.getEditTime(), val -> queryWrapper.and(User::getEditTime).ge(val));
        QueryWrapperUtils.applyIfNotNull(request.getCreateTime(), val -> queryWrapper.and(User::getCreateTime).ge(val));

        // 处理排序逻辑
        String sortField = StrUtil.trimToNull(request.getSortField());
        String column = sortField == null ? null : SORT_FIELD_MAP.get(sortField);
        boolean asc = StrUtil.equalsIgnoreCase(StrUtil.trimToNull(request.getSortOrder()), SORT_ORDER_ASC);
        if (column != null) {
            queryWrapper.orderBy(column, asc);
        } else {
            // 默认按创建时间倒序排列
            queryWrapper.orderBy(DEFAULT_SORT_COLUMN, false);
        }

        return page(page, queryWrapper);
    }

    /**
     * 获取用户详细信息（管理员权限）
     * 返回完整的用户实体对象，包含敏感信息
     *
     * @param id 用户ID
     * @param httpServletRequest HTTP请求对象
     * @return 用户详细信息
     * @throws BusinessException 当用户ID为空或用户不存在时抛出
     */
    @Override
    public User getUserDetail(Long id, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_MISSING, "用户ID不能为空");
        User user = getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        return user;
    }

    /**
     * 获取用户VO信息（普通用户权限）
     * 返回安全的用户视图对象，不包含敏感信息
     *
     * @param id 用户ID
     * @param httpServletRequest HTTP请求对象
     * @return 用户VO信息
     * @throws BusinessException 当用户ID为空或用户不存在时抛出
     */
    @Override
    public UserVO getUserVODetail(Long id, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_MISSING, "用户ID不能为空");
        User user = getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        UserVO vo = new UserVO();
        vo.copyUserInfoFrom(user);
        return vo;
    }

    // ===================== 用户自助相关方法 =====================

    /**
     * 更新当前登录用户的个人资料
     * 只允许更新非敏感信息，如用户名等
     *
     * @param request 个人信息更新请求
     * @param httpServletRequest HTTP请求对象
     * @return 更新是否成功
     * @throws BusinessException 当请求为空、用户未登录或更新失败时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateCurrentUserProfile(UserProfileUpdateRequest request, HttpServletRequest httpServletRequest) {
        validateRequest(request, "个人信息更新请求不能为空");
        // 获取当前登录用户
        User sessionUser = getSessionUserOrThrow(httpServletRequest);
        // 从数据库获取最新用户信息
        User dbUser = getById(sessionUser.getId());
        ThrowUtils.throwIf(dbUser == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在或已注销");

        // 复制非空属性到数据库用户对象
        BeanUtil.copyProperties(request, dbUser, CopyOptions.create().ignoreNullValue().ignoreError());

        boolean updated = updateById(dbUser);
        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "更新个人信息失败");
        // 更新session中的用户信息
        refreshSessionUser(httpServletRequest, dbUser);
        return Boolean.TRUE;
    }

    /**
     * 更新当前登录用户的邮箱
     * 需要验证密码和邮箱验证码，确保操作安全性
     *
     * @param request 邮箱更新请求
     * @param httpServletRequest HTTP请求对象
     * @return 更新是否成功
     * @throws BusinessException 当参数校验失败、验证码错误或更新失败时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateCurrentUserEmail(UserEmailUpdateRequest request, HttpServletRequest httpServletRequest) {
        validateRequest(request, "邮箱更新请求不能为空");
        User sessionUser = getSessionUserOrThrow(httpServletRequest);
        User dbUser = getById(sessionUser.getId());
        ThrowUtils.throwIf(dbUser == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在或已注销");

        String rawPassword = StrUtil.trimToNull(request.getUserPassword());
        String newEmail = StrUtil.trimToNull(request.getNewEmail());
        String code = StrUtil.trimToNull(request.getEmailCode());

        // 参数校验
        ThrowUtils.throwIf(StrUtil.isBlank(rawPassword), ErrorCode.PARAMS_MISSING, "密码不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(newEmail), ErrorCode.PARAMS_MISSING, "新邮箱不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(code), ErrorCode.PARAMS_MISSING, "验证码不能为空");
        ThrowUtils.throwIf(!Validator.isEmail(newEmail), ErrorCode.PARAMS_ERROR, "不支持的邮箱格式");
        ThrowUtils.throwIf(StrUtil.equalsIgnoreCase(newEmail, dbUser.getUserEmail()), ErrorCode.PARAMS_ERROR, "新邮箱不能与当前邮箱相同");
        ThrowUtils.throwIf(existsByColumn(UserFieldConstants.USER_EMAIL, newEmail, dbUser.getId()), ErrorCode.DATA_ALREADY_EXISTS, "邮箱已被占用");

        // 验证密码和验证码
        verifyPassword(dbUser, rawPassword);
        String redisKey = AuthConstants.buildEmailCodeKey(VerificationSceneEnum.RESET_PASSWORD, newEmail);
        validateAndConsumeCode(redisKey, code, "邮箱验证码已过期");

        // 更新邮箱
        dbUser.setUserEmail(newEmail);

        boolean updated = updateById(dbUser);
        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "更新邮箱失败");
        refreshSessionUser(httpServletRequest, dbUser);
        return Boolean.TRUE;
    }

    /**
     * 更新当前登录用户的手机号
     * 需要验证密码和短信验证码，确保操作安全性
     *
     * @param request 手机号更新请求
     * @param httpServletRequest HTTP请求对象
     * @return 更新是否成功
     * @throws BusinessException 当参数校验失败、验证码错误或更新失败时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateCurrentUserPhone(UserPhoneUpdateRequest request, HttpServletRequest httpServletRequest) {
        validateRequest(request, "手机号更新请求不能为空");
        User sessionUser = getSessionUserOrThrow(httpServletRequest);
        User dbUser = getById(sessionUser.getId());
        ThrowUtils.throwIf(dbUser == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在或已注销");

        String rawPassword = StrUtil.trimToNull(request.getUserPassword());
        String newPhone = StrUtil.trimToNull(request.getNewPhone());
        String code = StrUtil.trimToNull(request.getPhoneCode());

        // 参数校验
        ThrowUtils.throwIf(StrUtil.isBlank(rawPassword), ErrorCode.PARAMS_MISSING, "密码不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(newPhone), ErrorCode.PARAMS_MISSING, "新手机号不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(code), ErrorCode.PARAMS_MISSING, "验证码不能为空");
        ThrowUtils.throwIf(!Validator.isMobile(newPhone), ErrorCode.PARAMS_ERROR, "不支持的手机号格式");
        ThrowUtils.throwIf(StrUtil.equals(newPhone, dbUser.getUserPhone()), ErrorCode.PARAMS_ERROR, "新手机号不能与当前手机号相同");
        ThrowUtils.throwIf(existsByColumn(UserFieldConstants.USER_PHONE, newPhone, dbUser.getId()), ErrorCode.DATA_ALREADY_EXISTS, "手机号已被占用");

        // 验证密码和验证码
        verifyPassword(dbUser, rawPassword);
        String redisKey = AuthConstants.buildPhoneCodeKey(newPhone);
        validateAndConsumeCode(redisKey, code, "短信验证码已过期");

        // 更新手机号
        dbUser.setUserPhone(newPhone);
        boolean updated = updateById(dbUser);
        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "更新手机号失败");
        refreshSessionUser(httpServletRequest, dbUser);
        return Boolean.TRUE;
    }

    /**
     * 用户退出登录
     * 清除session和cookie中的登录状态
     *
     * @param httpServletRequest HTTP请求对象
     * @param httpServletResponse HTTP响应对象
     */
    @Override
    public void logout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        if (httpServletRequest == null) {
            return;
        }
        // 使session失效
        HttpSession session = httpServletRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        // 清除前端 cookie
        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        httpServletResponse.addCookie(cookie);
    }

    /**
     * 发送验证码
     * 根据场景类型和接收者自动选择发送渠道和业务策略
     *
     * @param request 验证码发送请求
     * @param httpServletRequest HTTP请求对象
     * @return 发送是否成功
     * @throws BusinessException 当请求为空或发送失败时抛出
     */
    @Override
    public Boolean sendCode(SendCodeRequest request, HttpServletRequest httpServletRequest) {
        validateRequest(request, "发送请求不能为空");
        // 自动识别渠道（EMAIL 或 PHONE）
        String channel = ChannelDetectorUtils.detectChannel(request.getReceiver());
        request.setChannel(channel);
        // 根据 scene 选择业务策略并执行发送逻辑
        sceneFactory.getStrategy(request.getScene()).execute(request);
        return Boolean.TRUE;
    }

    // ===================== 私有辅助方法 =====================

    /**
     * 判断指定字段值的用户是否已存在
     * <p>
     * 该方法根据传入的列名与值，动态构建查询条件，用于判断数据库中是否存在对应记录。
     * 常用于账号、邮箱、手机号等唯一性校验场景。
     * 若传入 {@code excludeId}，则在查询时排除该 ID 对应的用户记录，
     * 以避免在用户信息更新时与自身数据产生冲突。
     * </p>
     * <p><b>示例：</b></p>
     * <pre>{@code
     * // 检查邮箱是否已被占用
     * boolean exists = existsByColumn(UserFieldConstants.USER_EMAIL, "test@example.com", null);
     * }</pre>
     * <p><b>示例：</b></p>
     * <pre>{@code
     * // 更新用户信息时，排除自己
     * boolean exists = existsByColumn(UserFieldConstants.USER_EMAIL, "test@example.com", 1001L);
     * }</pre>
     *
     * @param column    字段名（仅支持账号、邮箱、手机号等定义在 {@link UserFieldConstants} 中的字段）
     * @param value     字段值，例如用户输入的账号、邮箱或手机号
     * @param excludeId 需要排除的用户 ID（用于更新场景，若为新增操作则传入 {@code null}）
     * @return 若存在相同字段值的记录，则返回 {@code true}；否则返回 {@code false}
     * @throws IllegalArgumentException 当传入的字段名不受支持时抛出
     */
    private boolean existsByColumn(String column, String value, Long excludeId) {
        // 构建基础查询条件：仅查询未逻辑删除的用户
        QueryWrapper queryWrapper = QueryWrapper.create().where(User::getIsDelete).eq(0);

        // 根据传入的列名动态拼接对应的字段查询条件
        switch (column) {
            case UserFieldConstants.USER_ACCOUNT ->
                // 检查用户账号是否存在
                    queryWrapper.and(User::getUserAccount).eq(value);
            case UserFieldConstants.USER_EMAIL ->
                // 检查邮箱是否存在
                    queryWrapper.and(User::getUserEmail).eq(value);
            case UserFieldConstants.USER_PHONE ->
                // 检查手机号是否存在
                    queryWrapper.and(User::getUserPhone).eq(value);
            default ->
                // 若传入了不支持的字段名，则抛出异常
                    throw new IllegalArgumentException("Unsupported column: " + column);
        }

        // 若指定了 excludeId，则在查询时排除该用户
        // 常用于"更新用户信息"时防止与自身数据重复
        if (excludeId != null) {
            queryWrapper.and(User::getId).ne(excludeId);
        }

        // 仅查询一条记录即可判断是否存在，提高性能
        queryWrapper.limit(1);

        // 执行查询：若能查到记录则返回 true，否则返回 false
        return getOne(queryWrapper) != null;
    }

    /**
     * 校验邮箱和手机号的格式与唯一性
     * <p>
     * 该方法可用于新增或更新用户场景：
     * <ul>
     *     <li>当 {@code excludeId} 为 {@code null} 时，用于新增用户（不排除自身）。</li>
     *     <li>当 {@code excludeId} 不为 {@code null} 时，用于更新用户（排除自身 ID）。</li>
     * </ul>
     * </p>
     *
     * @param email     邮箱地址，可为空
     * @param phone     手机号，可为空
     * @param excludeId 需要排除的用户 ID（更新场景使用；新增时传入 {@code null}）
     */
    private void validateEmailAndPhone(String email, String phone, Long excludeId) {
        // ======== 邮箱校验 ========
        if (StrUtil.isNotBlank(email)) {
            // 格式校验 - 使用hutool的验证器
            ThrowUtils.throwIf(!Validator.isEmail(email), ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
            // 唯一性校验 - 检查邮箱是否已被其他用户使用
            ThrowUtils.throwIf(
                    existsByColumn(UserFieldConstants.USER_EMAIL, email, excludeId),
                    ErrorCode.DATA_ALREADY_EXISTS,
                    "邮箱已被占用"
            );
        }

        // ======== 手机号校验 ========
        if (StrUtil.isNotBlank(phone)) {
            // 格式校验 - 使用hutool的验证器
            ThrowUtils.throwIf(!Validator.isMobile(phone), ErrorCode.PARAMS_ERROR, "手机号格式不正确");
            // 唯一性校验 - 检查手机号是否已被其他用户使用
            ThrowUtils.throwIf(
                    existsByColumn(UserFieldConstants.USER_PHONE, phone, excludeId),
                    ErrorCode.DATA_ALREADY_EXISTS,
                    "手机号已被占用"
            );
        }
    }

    /**
     * 从session中获取当前登录用户，如果未登录则抛出异常
     *
     * @param httpServletRequest HTTP请求对象
     * @return 当前登录用户
     * @throws BusinessException 当用户未登录时抛出
     */
    private User getSessionUserOrThrow(HttpServletRequest httpServletRequest) {
        Object user = httpServletRequest.getSession().getAttribute(UserConstants.USER_LOGIN_STATE);
        User currentUser = (User) user;
        ThrowUtils.throwIf(currentUser == null, ErrorCode.NOT_LOGIN_ERROR, "未登录或会话已失效");
        return currentUser;
    }

    /**
     * 保存用户登录状态到session
     *
     * @param httpServletRequest HTTP请求对象
     * @param user 用户实体对象
     */
    private void saveLoginState(HttpServletRequest httpServletRequest, User user) {
        if (httpServletRequest == null || user == null) {
            return;
        }
        // 创建或获取session并设置用户登录状态
        HttpSession session = httpServletRequest.getSession(true);
        session.setAttribute(UserConstants.USER_LOGIN_STATE, buildSafeUser(user));
        // 设置session超时时间
        session.setMaxInactiveInterval(UserConstants.SESSION_TIMEOUT_SECONDS);
    }

    /**
     * 刷新session中的用户信息
     * 当用户信息更新后调用此方法同步session
     *
     * @param httpServletRequest HTTP请求对象
     * @param user 更新后的用户实体对象
     */
    private void refreshSessionUser(HttpServletRequest httpServletRequest, User user) {
        if (httpServletRequest == null || user == null) {
            return;
        }
        HttpSession session = httpServletRequest.getSession(false);
        if (session == null) {
            return;
        }
        // 更新session中的用户信息
        session.setAttribute(UserConstants.USER_LOGIN_STATE, buildSafeUser(user));
    }

    /**
     * 构建安全的用户对象，移除敏感信息
     * 用于返回给前端或存储在session中
     *
     * @param user 原始用户实体对象
     * @return 安全的用户对象（不包含密码和盐值）
     */
    private User buildSafeUser(User user) {
        if (user == null) {
            return null;
        }
        // 复制用户信息但排除敏感字段
        User safeUser = new User();
        BeanUtil.copyProperties(user, safeUser, CopyOptions.create().ignoreNullValue().ignoreError());
        // 移除密码和盐值等敏感信息
        safeUser.setUserPassword(null);
        safeUser.setUserSalt(null);
        return safeUser;
    }

    /**
     * 验证用户密码是否正确
     *
     * @param user 用户实体对象
     * @param rawPassword 原始密码（未加密）
     * @throws BusinessException 当密码不正确时抛出
     */
    private void verifyPassword(User user, String rawPassword) {
        // 使用相同的加密方式验证密码
        String encrypted = DigestUtil.sha256Hex(rawPassword + user.getUserSalt());
        ThrowUtils.throwIf(!StrUtil.equals(encrypted, user.getUserPassword()),
                ErrorCode.PARAMS_ERROR, "密码不正确");
    }

    /**
     * 验证并消费验证码
     * 验证成功后会自动删除Redis中的验证码，防止重复使用
     *
     * @param redisKey Redis键名
     * @param providedCode 用户提供的验证码
     * @param expiredMessage 验证码过期时的错误消息
     * @throws BusinessException 当验证码不存在、已过期或不正确时抛出
     */
    private void validateAndConsumeCode(String redisKey, String providedCode, String expiredMessage) {
        // 从Redis获取缓存的验证码
        String cachedCode = stringRedisTemplate.opsForValue().get(redisKey);
        ThrowUtils.throwIf(StrUtil.isBlank(cachedCode), ErrorCode.LOGIN_EXPIRED, expiredMessage);
        // 比较验证码是否匹配
        ThrowUtils.throwIf(!StrUtil.equals(cachedCode, providedCode), ErrorCode.PARAMS_ERROR, "验证码不正确");
        // 验证成功后删除验证码，防止重复使用
        stringRedisTemplate.delete(redisKey);
    }

    // ===================== 核心业务辅助方法 =====================

    /**
     * 构建登录结果
     * 生成令牌、更新登录信息并组装返回数据
     *
     * @param user 用户实体对象
     * @param httpServletRequest HTTP请求对象，用于获取客户端IP
     * @return 用户视图对象
     * @throws BusinessException 当用户信息为空时抛出
     */
    private UserVO buildLoginResult(User user, HttpServletRequest httpServletRequest) {
        // 检查用户信息
        ThrowUtils.throwIf(user == null, ErrorCode.SYSTEM_ERROR, "用户信息为空");

        // 更新最后登录信息
        updateLastLoginInfo(user, httpServletRequest);

        // 保存登录态到session
        saveLoginState(httpServletRequest, user);

        // 复制用户基本信息到VO对象
        UserVO vo = new UserVO();
        vo.copyUserInfoFrom(buildSafeUser(user));
        return vo;
    }

    /**
     * 更新用户最后登录信息
     * 包括最后登录时间和登录IP地址
     *
     * @param user 用户实体对象
     * @param httpServletRequest HTTP请求对象，用于获取客户端IP
     */
    private void updateLastLoginInfo(User user, HttpServletRequest httpServletRequest) {
        LocalDateTime now = LocalDateTime.now();
        // 获取客户端IP地址
        String ipAddress = IpUtils.getClientIp(httpServletRequest);

        // 使用MyBatis-Flex的UpdateChain更新数据库中的登录信息
        boolean success = UpdateChain.of(userMapper)
                .set(User::getLastLoginTime, now)
                .set(User::getLastLoginIp, ipAddress)
                .where(User::getId).eq(user.getId())
                .update();

        if (!success) {
            log.warn("更新用户最后登录时间失败，用户ID={}", user.getId());
        }

        // 更新内存中的用户对象，保持数据一致性
        user.setLastLoginTime(now);
    }

    /**
     * 生成唯一的邀请码
     * 采用重试机制确保生成的邀请码不重复
     *
     * @return 唯一的邀请码
     * @throws BusinessException 当多次重试后仍无法生成唯一邀请码时抛出
     */
    private String generateUniqueInviteCode() {
        // 重试机制，避免邀请码冲突
        for (int i = 0; i < UserConstants.INVITE_CODE_MAX_RETRY; i++) {
            // 从指定字符池中随机生成指定长度的邀请码
            String code = RandomUtil.randomString(UserConstants.INVITE_CODE_CHAR_POOL, UserConstants.INVITE_CODE_LENGTH);
            // 检查邀请码是否已存在
            boolean exists = getOne(new QueryWrapper().where(User::getInviteCode).eq(code).limit(1)) != null;
            if (!exists) {
                return code;
            }
        }
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成邀请码失败，请稍后重试");
    }

    /**
     * 生成用户盐值
     * 用于密码加密，增强安全性
     *
     * @return 十六进制表示的盐值
     */
    private String generateUserSalt() {
        // 生成随机字节数组作为盐值
        byte[] saltBytes = RandomUtil.randomBytes(UserConstants.USER_SALT_BYTE_LENGTH);
        // 转换为十六进制字符串存储
        return HexUtil.encodeHexStr(saltBytes);
    }

    /**
     * 构建默认用户名
     * 当用户未提供用户名时自动生成
     *
     * @param userAccount 用户账号
     * @return 默认用户名
     */
    private String buildDefaultUserName(String userAccount) {
        return UserConstants.DEFAULT_USERNAME_PREFIX + userAccount;
    }

    /**
     * 验证请求对象是否为空
     *
     * @param request 请求对象
     * @param message 错误消息
     * @throws BusinessException 当请求对象为空时抛出
     */
    private void validateRequest(Object request, String message) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_MISSING, message);
    }

    /**
     * 设置用户的 VIP 开始和结束时间
     * <p>
     * 当用户被标记为 VIP 时，会自动设置当前时间为开始时间，
     * 并通过 {@link VipTimeUtils#calculateVipEndTime(LocalDateTime)} 计算到期时间。
     * 若用户不是 VIP，则不修改任何时间字段。
     * </p>
     *
     * @param isVip VIP状态
     * @param user 用户实体对象，不能为空
     */
    private void applyVipTimeIfNeeded(Integer isVip, User user) {
        // 判空防御
        if (user == null) {
            return;
        }
        // 若用户状态为 VIP，则设置起止时间
        if (VipStatusEnum.VIP.getValue().equals(isVip)) {
            LocalDateTime now = LocalDateTime.now();
            user.setVipStartTime(now);
            // 使用工具类计算VIP结束时间
            user.setVipEndTime(VipTimeUtils.calculateVipEndTime(now));
        }
    }
}