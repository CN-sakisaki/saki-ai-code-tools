package com.saki.sakiaicodetoolsbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
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
import com.saki.sakiaicodetoolsbackend.context.UserContext;
import com.saki.sakiaicodetoolsbackend.exception.BusinessException;
import com.saki.sakiaicodetoolsbackend.exception.ErrorCode;
import com.saki.sakiaicodetoolsbackend.exception.ThrowUtils;
import com.saki.sakiaicodetoolsbackend.mapper.UserMapper;
import com.saki.sakiaicodetoolsbackend.model.dto.admin.user.UserAddRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.admin.user.UserDeleteRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.admin.user.UserQueryRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.admin.user.UserUpdateRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.login.LoginRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.login.RegisterRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.login.TokenRefreshRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.user.UserEmailUpdateRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.user.UserPhoneUpdateRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.user.UserProfileUpdateRequest;
import com.saki.sakiaicodetoolsbackend.model.entity.User;
import com.saki.sakiaicodetoolsbackend.model.enums.LoginTypeEnum;
import com.saki.sakiaicodetoolsbackend.model.vo.UserVO;
import com.saki.sakiaicodetoolsbackend.service.UserService;
import com.saki.sakiaicodetoolsbackend.service.login.LoginStrategyFactory;
import com.saki.sakiaicodetoolsbackend.service.mail.MailService;
import com.saki.sakiaicodetoolsbackend.utils.IpUtils;
import com.saki.sakiaicodetoolsbackend.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户服务实现类，负责用户登录、令牌管理、邮件验证码发送等核心业务逻辑。
 * 继承MyBatis-Flex的ServiceImpl，提供基础的CRUD操作，并实现自定义的用户服务接口。
 *
 * @author saki酱
 * @version 1.0
 * @since 2025-10-17 11:53
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final String SORT_ORDER_ASC = "ascend";
    private static final String DEFAULT_SORT_COLUMN = "create_time";
    private static final Map<String, String> SORT_FIELD_MAP = Map.ofEntries(
            Map.entry("id", "id"),
            Map.entry("createTime", "create_time"),
            Map.entry("updateTime", "update_time"),
            Map.entry("lastLoginTime", "last_login_time"),
            Map.entry("userAccount", "user_account"),
            Map.entry("userName", "user_name"),
            Map.entry("userRole", "user_role"),
            Map.entry("userStatus", "user_status"),
            Map.entry("isVip", "is_vip")
    );

    private static final CopyOptions COPY_NON_NULL_OPTIONS = CopyOptions.create().ignoreNullValue().ignoreError();

    private static final String COLUMN_USER_ACCOUNT = "user_account";
    private static final String COLUMN_USER_EMAIL = "user_email";
    private static final String COLUMN_USER_PHONE = "user_phone";

    
    /** 登录策略工厂，用于根据不同登录类型选择认证策略 */
    private final LoginStrategyFactory loginStrategyFactory;

    /** JWT工具类，用于令牌的生成和解析 */
    private final JwtUtils jwtUtils;

    /** Redis模板，用于存储刷新令牌和验证码 */
    private final StringRedisTemplate redisTemplate;

    /** 发送邮件服务 */
    private final MailService mailService;


    // ===================== 登录相关方法 =====================

    /**
     * 用户登录方法，支持多种登录方式。
     * 根据登录类型选择相应的认证策略，认证成功后生成令牌并更新登录信息。
     *
     * @param request 登录请求对象，包含登录类型、凭证等信息
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

        // 使用策略模式进行用户认证
        User user = loginStrategyFactory.getStrategy(loginType).authenticate(request);

        // 构建登录结果
        return buildLoginResult(user, httpServletRequest);
    }

    /**
     * 用户注册。
     *
     * @param request 注册请求对象
     * @return 新用户的主键 ID
     */
    @Override
    public Long register(RegisterRequest request) {
        validateRequest(request, "注册请求不能为空");

        String userAccount = StrUtil.trim(request.getUserAccount());
        String password = request.getUserPassword();
        String confirmPassword = request.getConfirmPassword();

        // 基本参数校验
        ThrowUtils.throwIf(StrUtil.isBlank(userAccount) || StrUtil.isBlank(password) || StrUtil.isBlank(confirmPassword),
                ErrorCode.PARAMS_MISSING, "账号或密码不能为空");
        ThrowUtils.throwIf(!StrUtil.equals(password, confirmPassword), ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        ThrowUtils.throwIf(userAccount.length() < 8 || password.length() < 8, ErrorCode.PARAMS_ERROR, "账号或密码长度至少8位");

        // 校验账号是否已存在
        boolean accountExists = getOne(new QueryWrapper().where(User::getUserAccount).eq(userAccount).limit(1)) != null;
        ThrowUtils.throwIf(accountExists, ErrorCode.PARAMS_ERROR, "账号已存在");

        String salt = generateUserSalt();
        String encryptedPassword = DigestUtil.sha256Hex(password + salt);

        String inviteCode = Optional.ofNullable(StrUtil.trimToNull(request.getInviteCode()))
                .orElseGet(this::generateUniqueInviteCode);

        LocalDateTime now = LocalDateTime.now();

        User newUser = User.builder()
                .userAccount(userAccount)
                .userPassword(encryptedPassword)
                .userName(buildDefaultUserName(userAccount))
                .userAvatar(UserConstants.DEFAULT_AVATAR_PATH)
                .userProfile(UserConstants.DEFAULT_PROFILE)
                .userRole(UserConstants.DEFAULT_USER_ROLE)
                .userStatus(UserConstants.DEFAULT_USER_STATUS)
                .isVip(UserConstants.DEFAULT_VIP_STATUS)
                .inviteCode(inviteCode)
                .userSalt(salt)
                .createTime(now)
                .updateTime(now)
                .build();

        boolean saved = save(newUser);
        ThrowUtils.throwIf(!saved, ErrorCode.SYSTEM_ERROR, "用户注册失败");
        return newUser.getId();
    }

    // ===================== 管理员相关方法 =====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(UserAddRequest request) {
        validateRequest(request, "新增用户请求不能为空");

        String userAccount = StrUtil.trimToNull(request.getUserAccount());
        String rawPassword = StrUtil.trimToNull(request.getUserPassword());
        ThrowUtils.throwIf(StrUtil.isBlank(userAccount), ErrorCode.PARAMS_MISSING, "用户账号不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(rawPassword), ErrorCode.PARAMS_MISSING, "用户密码不能为空");
        ThrowUtils.throwIf(userAccount.length() < 8 || rawPassword.length() < 8,
                ErrorCode.PARAMS_ERROR, "账号和密码长度至少8位");

        ThrowUtils.throwIf(existsByColumn(COLUMN_USER_ACCOUNT, userAccount, null),
                ErrorCode.DATA_ALREADY_EXISTS, "账号已存在");

        String email = StrUtil.trimToNull(request.getUserEmail());
        if (StrUtil.isNotBlank(email)) {
            ThrowUtils.throwIf(existsByColumn(COLUMN_USER_EMAIL, email, null),
                    ErrorCode.DATA_ALREADY_EXISTS, "邮箱已被占用");
        }

        String phone = StrUtil.trimToNull(request.getUserPhone());
        if (StrUtil.isNotBlank(phone)) {
            ThrowUtils.throwIf(existsByColumn(COLUMN_USER_PHONE, phone, null),
                    ErrorCode.DATA_ALREADY_EXISTS, "手机号已被占用");
        }

        String salt = generateUserSalt();
        String encryptedPassword = DigestUtil.sha256Hex(rawPassword + salt);
        String inviteCode = generateUniqueInviteCode();
        LocalDateTime now = LocalDateTime.now();

        User user = new User();
        BeanUtil.copyProperties(request, user, COPY_NON_NULL_OPTIONS);
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptedPassword);
        user.setUserSalt(salt);
        user.setInviteCode(inviteCode);
        user.setUserEmail(email);
        user.setUserPhone(phone);
        user.setUserName(StrUtil.blankToDefault(user.getUserName(), buildDefaultUserName(userAccount)));
        user.setUserAvatar(StrUtil.blankToDefault(user.getUserAvatar(), UserConstants.DEFAULT_AVATAR_PATH));
        user.setUserProfile(StrUtil.blankToDefault(user.getUserProfile(), UserConstants.DEFAULT_PROFILE));
        user.setUserRole(StrUtil.blankToDefault(user.getUserRole(), UserConstants.DEFAULT_USER_ROLE));
        user.setUserStatus(Optional.ofNullable(user.getUserStatus()).orElse(UserConstants.DEFAULT_USER_STATUS));
        user.setIsVip(Optional.ofNullable(user.getIsVip()).orElse(UserConstants.DEFAULT_VIP_STATUS));
        user.setCreateTime(now);
        user.setUpdateTime(now);
        user.setEditTime(now);
        user.setIsDelete(0);

        boolean saved = save(user);
        ThrowUtils.throwIf(!saved, ErrorCode.DATA_SAVE_FAILED, "新增用户失败");
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteUsers(UserDeleteRequest request) {
        validateRequest(request, "删除用户请求不能为空");
        List<Long> ids = request.getIds();
        ThrowUtils.throwIf(CollUtil.isEmpty(ids), ErrorCode.PARAMS_MISSING, "待删除的用户ID不能为空");

        List<User> users = listByIds(ids);
        Set<Long> existingIds = users.stream()
                .map(User::getId)
                .collect(Collectors.toSet());
        List<Long> missingIds = ids.stream()
                .filter(id -> !existingIds.contains(id))
                .collect(Collectors.toList());
        ThrowUtils.throwIf(CollUtil.isNotEmpty(missingIds), ErrorCode.NOT_FOUND_ERROR,
                "部分用户不存在，ID=" + missingIds.stream().map(String::valueOf).collect(Collectors.joining(",")));

        boolean removed = removeByIds(ids);
        ThrowUtils.throwIf(!removed, ErrorCode.DATA_DELETE_FAILED, "删除用户失败");
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateUser(UserUpdateRequest request) {
        validateRequest(request, "更新用户请求不能为空");
        Long id = request.getId();
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_MISSING, "用户ID不能为空");

        User existingUser = getById(id);
        ThrowUtils.throwIf(existingUser == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");

        if (StrUtil.isNotBlank(request.getUserEmail())) {
            ThrowUtils.throwIf(existsByColumn(COLUMN_USER_EMAIL, StrUtil.trim(request.getUserEmail()), id),
                    ErrorCode.DATA_ALREADY_EXISTS, "邮箱已被占用");
        }
        if (StrUtil.isNotBlank(request.getUserPhone())) {
            ThrowUtils.throwIf(existsByColumn(COLUMN_USER_PHONE, StrUtil.trim(request.getUserPhone()), id),
                    ErrorCode.DATA_ALREADY_EXISTS, "手机号已被占用");
        }

        User updateUser = new User();
        BeanUtil.copyProperties(request, updateUser, COPY_NON_NULL_OPTIONS);
        updateUser.setId(id);
        LocalDateTime now = LocalDateTime.now();
        updateUser.setUpdateTime(now);
        if (updateUser.getEditTime() == null) {
            updateUser.setEditTime(now);
        }

        boolean updated = updateById(updateUser);
        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "更新用户失败");
        return Boolean.TRUE;
    }

    @Override
    public Page<User> listUsersByPage(UserQueryRequest request) {
        validateRequest(request, "分页查询请求不能为空");

        Page<User> page = new Page<>(request.getPageNum(), request.getPageSize());
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(User::getIsDelete).eq(0);

        if (StrUtil.isNotBlank(request.getUserAccount())) {
            queryWrapper.and(User::getUserAccount).like(StrUtil.trim(request.getUserAccount()));
        }
        if (StrUtil.isNotBlank(request.getUserName())) {
            queryWrapper.and(User::getUserName).like(StrUtil.trim(request.getUserName()));
        }
        if (StrUtil.isNotBlank(request.getUserEmail())) {
            queryWrapper.and(User::getUserEmail).like(StrUtil.trim(request.getUserEmail()));
        }
        if (StrUtil.isNotBlank(request.getUserPhone())) {
            queryWrapper.and(User::getUserPhone).like(StrUtil.trim(request.getUserPhone()));
        }
        if (StrUtil.isNotBlank(request.getUserRole())) {
            queryWrapper.and(User::getUserRole).eq(StrUtil.trim(request.getUserRole()));
        }
        if (request.getUserStatus() != null) {
            queryWrapper.and(User::getUserStatus).eq(request.getUserStatus());
        }
        if (request.getIsVip() != null) {
            queryWrapper.and(User::getIsVip).eq(request.getIsVip());
        }
        if (request.getVipStartTime() != null) {
            queryWrapper.and(User::getVipStartTime).ge(request.getVipStartTime());
        }
        if (request.getVipEndTime() != null) {
            queryWrapper.and(User::getVipEndTime).le(request.getVipEndTime());
        }
        if (request.getLastLoginTime() != null) {
            queryWrapper.and(User::getLastLoginTime).ge(request.getLastLoginTime());
        }
        if (request.getEditTime() != null) {
            queryWrapper.and(User::getEditTime).ge(request.getEditTime());
        }
        if (request.getCreateTime() != null) {
            queryWrapper.and(User::getCreateTime).ge(request.getCreateTime());
        }

        String sortField = StrUtil.trimToNull(request.getSortField());
        String column = sortField == null ? null : SORT_FIELD_MAP.get(sortField);
        boolean asc = StrUtil.equalsIgnoreCase(StrUtil.trimToNull(request.getSortOrder()), SORT_ORDER_ASC);
        if (column != null) {
            queryWrapper.orderBy(column, asc);
        } else {
            queryWrapper.orderBy(DEFAULT_SORT_COLUMN, false);
        }

        return page(page, queryWrapper);
    }

    @Override
    public User getUserDetail(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_MISSING, "用户ID不能为空");
        User user = getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        return user;
    }

    // ===================== 用户自助相关方法 =====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateCurrentUserProfile(UserProfileUpdateRequest request) {
        validateRequest(request, "个人信息更新请求不能为空");
        User currentUser = getCurrentUserOrThrow();

        User updateUser = new User();
        BeanUtil.copyProperties(request, updateUser, COPY_NON_NULL_OPTIONS);
        updateUser.setId(currentUser.getId());
        LocalDateTime now = LocalDateTime.now();
        updateUser.setEditTime(now);
        updateUser.setUpdateTime(now);

        boolean updated = updateById(updateUser);
        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "更新个人信息失败");
        refreshUserContext(currentUser, updateUser);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateCurrentUserEmail(UserEmailUpdateRequest request) {
        validateRequest(request, "邮箱更新请求不能为空");
        User currentUser = getCurrentUserOrThrow();

        String rawPassword = StrUtil.trimToNull(request.getUserPassword());
        String newEmail = StrUtil.trimToNull(request.getNewEmail());
        String code = StrUtil.trimToNull(request.getEmailCode());

        ThrowUtils.throwIf(StrUtil.isBlank(rawPassword), ErrorCode.PARAMS_MISSING, "密码不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(newEmail), ErrorCode.PARAMS_MISSING, "新邮箱不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(code), ErrorCode.PARAMS_MISSING, "验证码不能为空");

        verifyPassword(currentUser, rawPassword);
        ThrowUtils.throwIf(StrUtil.equalsIgnoreCase(newEmail, currentUser.getUserEmail()),
                ErrorCode.PARAMS_ERROR, "新邮箱不能与当前邮箱相同");
        ThrowUtils.throwIf(existsByColumn(COLUMN_USER_EMAIL, newEmail, currentUser.getId()),
                ErrorCode.DATA_ALREADY_EXISTS, "邮箱已被占用");

        String redisKey = AuthConstants.buildEmailCodeKey(newEmail);
        validateAndConsumeCode(redisKey, code, "邮箱验证码已过期");

        User updateUser = new User();
        updateUser.setId(currentUser.getId());
        updateUser.setUserEmail(newEmail);
        updateUser.setUpdateTime(LocalDateTime.now());

        boolean updated = updateById(updateUser);
        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "更新邮箱失败");
        refreshUserContext(currentUser, updateUser);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateCurrentUserPhone(UserPhoneUpdateRequest request) {
        validateRequest(request, "手机号更新请求不能为空");
        User currentUser = getCurrentUserOrThrow();

        String rawPassword = StrUtil.trimToNull(request.getUserPassword());
        String newPhone = StrUtil.trimToNull(request.getNewPhone());
        String code = StrUtil.trimToNull(request.getPhoneCode());

        ThrowUtils.throwIf(StrUtil.isBlank(rawPassword), ErrorCode.PARAMS_MISSING, "密码不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(newPhone), ErrorCode.PARAMS_MISSING, "新手机号不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(code), ErrorCode.PARAMS_MISSING, "验证码不能为空");

        verifyPassword(currentUser, rawPassword);
        ThrowUtils.throwIf(StrUtil.equals(newPhone, currentUser.getUserPhone()),
                ErrorCode.PARAMS_ERROR, "新手机号不能与当前手机号相同");
        ThrowUtils.throwIf(existsByColumn(COLUMN_USER_PHONE, newPhone, currentUser.getId()),
                ErrorCode.DATA_ALREADY_EXISTS, "手机号已被占用");

        String redisKey = AuthConstants.buildPhoneCodeKey(newPhone);
        validateAndConsumeCode(redisKey, code, "短信验证码已过期");

        User updateUser = new User();
        updateUser.setId(currentUser.getId());
        updateUser.setUserPhone(newPhone);
        updateUser.setUpdateTime(LocalDateTime.now());

        boolean updated = updateById(updateUser);
        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "更新手机号失败");
        refreshUserContext(currentUser, updateUser);
        return Boolean.TRUE;
    }

    private boolean existsByColumn(String column, String value, Long excludeId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(User::getIsDelete).eq(0);
        switch (column) {
            case COLUMN_USER_ACCOUNT -> queryWrapper.and(User::getUserAccount).eq(value);
            case COLUMN_USER_EMAIL -> queryWrapper.and(User::getUserEmail).eq(value);
            case COLUMN_USER_PHONE -> queryWrapper.and(User::getUserPhone).eq(value);
            default -> throw new IllegalArgumentException("Unsupported column: " + column);
        }
        if (excludeId != null) {
            queryWrapper.and(User::getId).ne(excludeId);
        }
        queryWrapper.limit(1);
        return getOne(queryWrapper) != null;
    }

    private User getCurrentUserOrThrow() {
        User currentUser = UserContext.getUser();
        ThrowUtils.throwIf(currentUser == null, ErrorCode.NOT_LOGIN_ERROR, "未登录或会话已失效");
        return currentUser;
    }

    private void verifyPassword(User user, String rawPassword) {
        String encrypted = DigestUtil.sha256Hex(rawPassword + user.getUserSalt());
        ThrowUtils.throwIf(!StrUtil.equals(encrypted, user.getUserPassword()),
                ErrorCode.PARAMS_ERROR, "密码不正确");
    }

    private void validateAndConsumeCode(String redisKey, String providedCode, String expiredMessage) {
        String cachedCode = redisTemplate.opsForValue().get(redisKey);
        ThrowUtils.throwIf(StrUtil.isBlank(cachedCode), ErrorCode.LOGIN_EXPIRED, expiredMessage);
        ThrowUtils.throwIf(!StrUtil.equals(cachedCode, providedCode), ErrorCode.PARAMS_ERROR, "验证码不正确");
        redisTemplate.delete(redisKey);
    }

    private void refreshUserContext(User currentUser, User updatedValues) {
        if (currentUser == null || updatedValues == null) {
            return;
        }
        BeanUtil.copyProperties(updatedValues, currentUser, COPY_NON_NULL_OPTIONS);
        UserContext.setUser(currentUser);
    }

    // ===================== 邮箱验证码相关方法 =====================

    /**
     * 发送邮箱登录验证码。
     * 生成6位随机验证码，存储到Redis并发送到用户邮箱。
     *
     * @param request 登录请求对象，必须包含用户邮箱
     * @throws BusinessException 当邮箱为空、用户不存在或邮件发送失败时抛出
     */
    @Override
    public void sendEmailLoginCode(LoginRequest request) {
        // 验证请求参数
        validateRequest(request, "请求参数不能为空");
        String email = request.getUserEmail();

        // 检查邮箱是否为空
        ThrowUtils.throwIf(StrUtil.isBlank(email), ErrorCode.PARAMS_MISSING, "邮箱不能为空");

        // 查询用户是否存在
        User user = getOne(new QueryWrapper().where(User::getUserEmail).eq(email).limit(1));
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "邮箱未注册");

        // 生成6位随机验证码
        String code = RandomUtil.randomNumbers(6);
        String redisKey = AuthConstants.buildEmailCodeKey(email);

        // 将验证码存储到Redis，设置5分钟过期时间
        redisTemplate.opsForValue().set(redisKey, code, Duration.ofMinutes(AuthConstants.EMAIL_CODE_EXPIRE_MINUTES));

        try {
            // 发送验证码邮件
            mailService.sendEmailCode(email, code);
        } catch (Exception ex) {
            // 如果邮件发送失败，删除Redis中的验证码
            redisTemplate.delete(redisKey);
            log.error("发送登录验证码失败: {}", ex.getMessage(), ex);
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAILED, "发送验证码失败，请稍后重试");
        }
    }

    // ===================== Token刷新相关方法 =====================

    /**
     * 刷新访问令牌。
     * 使用刷新令牌验证用户身份，生成新地访问令牌。
     *
     * @param request 令牌刷新请求对象，必须包含访问令牌
     * @return 新地访问令牌
     * @throws BusinessException 当请求参数为空、访问令牌无效或刷新令牌不存在时抛出
     */
    @Override
    public String refreshAccessToken(TokenRefreshRequest request) {
        // 验证请求参数
        validateRequest(request, "请求参数不能为空");
        String accessToken = request.getAccessToken();
        ThrowUtils.throwIf(StrUtil.isBlank(accessToken), ErrorCode.PARAMS_MISSING, "AccessToken 不能为空");

        // 解析访问令牌（允许过期）
        Claims claims = jwtUtils.parseTokenAllowExpired(accessToken);

        // 从subject中解析用户ID
        Long userId = parseUserId(claims.getSubject());
        String refreshTokenKey = AuthConstants.buildRefreshTokenKey(userId);

        // 从Redis获取刷新令牌
        String refreshToken = redisTemplate.opsForValue().get(refreshTokenKey);

        // 验证刷新令牌是否存在且有效
        ThrowUtils.throwIf(StrUtil.isBlank(refreshToken) || !jwtUtils.isTokenValid(refreshToken),
                ErrorCode.LOGIN_EXPIRED, "登录状态已过期，请重新登录");

        // 获取用户角色：优先从令牌中获取，其次从数据库获取，最后使用默认角色
        String userRole = Optional.ofNullable(claims.get("userRole", String.class))
                .filter(StrUtil::isNotBlank)
                .orElseGet(() -> Optional.ofNullable(getById(userId))
                        .map(User::getUserRole)
                        .filter(StrUtil::isNotBlank)
                        .orElse(UserConstants.DEFAULT_USER_ROLE));

        // 生成新地访问令牌
        return generateAndStoreTokens(userId, userRole).getAccessToken();
    }

    // ===================== 辅助方法 =====================

    /**
     * 构建登录结果。
     * 生成令牌、更新登录信息并组装返回数据。
     *
     * @param user 用户实体对象
     * @param httpServletRequest HTTP请求对象，用于获取客户端IP
     * @return 用户视图对象
     * @throws BusinessException 当用户信息为空时抛出
     */
    private UserVO buildLoginResult(User user, HttpServletRequest httpServletRequest) {
        // 检查用户信息
        ThrowUtils.throwIf(user == null, ErrorCode.SYSTEM_ERROR, "用户信息为空");

        // 使用用户角色或默认角色
        String role = StrUtil.blankToDefault(user.getUserRole(), UserConstants.DEFAULT_USER_ROLE);

        // 生成令牌
        UserVO vo = generateAndStoreTokens(user.getId(), role);

        // 更新最后登录信息
        updateLastLoginInfo(user, httpServletRequest);

        // 复制用户基本信息
        vo.copyUserInfoFrom(user);
        return vo;
    }

    /**
     * 生成并存储令牌。
     * 生成访问令牌和刷新令牌，将刷新令牌存储到Redis。
     *
     * @param userId 用户ID
     * @param role 用户角色
     * @return 包含访问令牌的用户视图对象
     */
    private UserVO generateAndStoreTokens(Long userId, String role) {
        // 生成访问令牌和刷新令牌
        String accessToken = jwtUtils.generateAccessToken(userId, role);
        String refreshToken = jwtUtils.generateRefreshToken(userId, role);

        // 将刷新令牌存储到Redis
        redisTemplate.opsForValue().set(AuthConstants.buildRefreshTokenKey(userId), refreshToken, jwtUtils.getRefreshTokenExpireDuration());

        // 组装返回结果
        UserVO vo = new UserVO();
        vo.setAccessToken(accessToken);
        return vo;
    }

    /**
     * 更新用户最后登录信息。
     * 包括最后登录时间和登录IP地址。
     *
     * @param user 用户实体对象
     * @param httpServletRequest HTTP请求对象，用于获取客户端IP
     */
    private void updateLastLoginInfo(User user, HttpServletRequest httpServletRequest) {
        LocalDateTime now = LocalDateTime.now();
        // 获取客户端IP地址
        String ipAddress = IpUtils.getClientIp(httpServletRequest);

        // 更新数据库中的登录信息
        boolean success = UpdateChain.of(User.class)
                .set(User::getLastLoginTime, now)
                .set(User::getLastLoginIp, ipAddress)
                .where(User::getId).eq(user.getId())
                .update();

        if (!success) {
            log.warn("更新用户最后登录时间失败，用户ID={}", user.getId());
        }

        // 更新内存中的用户对象
        user.setLastLoginTime(now);
    }

    /**
     * 生成唯一的邀请码。
     *
     * @return 唯一的邀请码
     */
    private String generateUniqueInviteCode() {
        for (int i = 0; i < UserConstants.INVITE_CODE_MAX_RETRY; i++) {
            String code = RandomUtil.randomString(UserConstants.INVITE_CODE_CHAR_POOL, UserConstants.INVITE_CODE_LENGTH);
            boolean exists = getOne(new QueryWrapper().where(User::getInviteCode).eq(code).limit(1)) != null;
            if (!exists) {
                return code;
            }
        }
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成邀请码失败，请稍后重试");
    }

    /**
     * 生成用户盐值。
     *
     * @return 十六进制表示的盐值
     */
    private String generateUserSalt() {
        byte[] saltBytes = RandomUtil.randomBytes(UserConstants.USER_SALT_BYTE_LENGTH);
        return HexUtil.encodeHexStr(saltBytes);
    }

    /**
     * 构建默认用户名。
     *
     * @param userAccount 用户账号
     * @return 默认用户名
     */
    private String buildDefaultUserName(String userAccount) {
        return UserConstants.DEFAULT_USERNAME_PREFIX + userAccount;
    }



    /**
     * 验证请求对象是否为空。
     *
     * @param request 请求对象
     * @param message 错误消息
     * @throws BusinessException 当请求对象为空时抛出
     */
    private void validateRequest(Object request, String message) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_MISSING, message);
    }

    /**
     * 解析用户ID。
     * 将JWT主题字符串转换为Long类型的用户ID。
     *
     * @param subject JWT主题（用户ID字符串）
     * @return 用户ID
     * @throws BusinessException 当用户ID格式不正确时抛出
     */
    private Long parseUserId(String subject) {
        try {
            return Long.valueOf(subject);
        } catch (NumberFormatException ex) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID, "非法的用户标识");
        }
    }
}