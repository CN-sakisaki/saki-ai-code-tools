package com.saki.sakiaicodetoolsbackend.service.impl;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.saki.sakiaicodetoolsbackend.constant.AuthConstants;
import com.saki.sakiaicodetoolsbackend.constant.UserConstants;
import com.saki.sakiaicodetoolsbackend.exception.BusinessException;
import com.saki.sakiaicodetoolsbackend.exception.ErrorCode;
import com.saki.sakiaicodetoolsbackend.exception.ThrowUtils;
import com.saki.sakiaicodetoolsbackend.mapper.UserMapper;
import com.saki.sakiaicodetoolsbackend.model.dto.LoginRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.RegisterRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.TokenRefreshRequest;
import com.saki.sakiaicodetoolsbackend.model.entity.User;
import com.saki.sakiaicodetoolsbackend.model.enums.LoginTypeEnum;
import com.saki.sakiaicodetoolsbackend.model.vo.UserVO;
import com.saki.sakiaicodetoolsbackend.service.UserService;
import com.saki.sakiaicodetoolsbackend.service.login.LoginStrategyFactory;
import com.saki.sakiaicodetoolsbackend.utils.IpUtils;
import com.saki.sakiaicodetoolsbackend.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

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

    // ===================== 常量定义 =====================

    /** 邮件主题 */
    private static final String EMAIL_SUBJECT = "登录验证码";

    /** 邮件模板路径 */
    private static final String EMAIL_TEMPLATE_PATH = "templates/login-code.html";

    // ===================== 依赖注入 =====================

    /** 登录策略工厂，用于根据不同登录类型选择认证策略 */
    private final LoginStrategyFactory loginStrategyFactory;

    /** JWT工具类，用于令牌的生成和解析 */
    private final JwtUtils jwtUtils;

    /** Redis模板，用于存储刷新令牌和验证码 */
    private final StringRedisTemplate redisTemplate;

    /** 邮件发送器，用于发送验证码邮件 */
    private final JavaMailSender mailSender;

    /** 邮件模板缓存，使用原子引用保证线程安全 */
    private final AtomicReference<String> emailTemplateCache = new AtomicReference<>();

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
    public UserVO login(LoginRequest request) {
        // 验证请求参数
        validateRequest(request, "请求参数不能为空");

        // 解析登录类型，如果不存在则抛出异常
        LoginTypeEnum loginType = LoginTypeEnum.fromValue(request.getLoginType())
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的登录方式"));

        // 使用策略模式进行用户认证
        User user = loginStrategyFactory.getStrategy(loginType).authenticate(request);

        // 构建登录结果
        return buildLoginResult(user, request.getHttpServletRequest());
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
            sendEmailCode(email, code);
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
     * 发送验证码邮件。
     *
     * @param email 目标邮箱地址
     * @param code 验证码
     * @throws MessagingException 当邮件发送失败时抛出
     * @throws IOException 当邮件模板读取失败时抛出
     */
    private void sendEmailCode(String email, String code) throws MessagingException, IOException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());
        helper.setTo(email);
        helper.setSubject(EMAIL_SUBJECT);
        // 构建邮件内容（HTML格式）
        helper.setText(buildEmailContent(code), true);
        mailSender.send(message);
    }

    /**
     * 构建邮件内容。
     * 使用模板文件或默认模板，替换验证码和过期时间占位符。
     *
     * @param code 验证码
     * @return 构建完成的邮件内容
     * @throws IOException 当模板文件读取失败时抛出
     */
    private String buildEmailContent(String code) throws IOException {
        // 使用缓存或加载邮件模板
        String template = emailTemplateCache.updateAndGet(current -> {
            if (StrUtil.isBlank(current)) {
                try {
                    return loadTemplate();
                } catch (IOException e) {
                    log.error("加载邮件模板失败", e);
                    return defaultTemplate();
                }
            }
            return current;
        });

        // 替换模板中的占位符
        return template.replace("${code}", code)
                .replace("${expireMinutes}", String.valueOf(AuthConstants.EMAIL_CODE_EXPIRE_MINUTES));
    }

    /**
     * 加载邮件模板文件。
     *
     * @return 模板文件内容
     * @throws IOException 当模板文件不存在或读取失败时抛出
     */
    private String loadTemplate() throws IOException {
        ClassPathResource resource = new ClassPathResource(EMAIL_TEMPLATE_PATH);
        if (!resource.exists()) {
            // 如果模板文件不存在，返回默认模板
            return defaultTemplate();
        }
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

    /**
     * 获取默认邮件模板。
     *
     * @return 默认模板内容
     */
    private String defaultTemplate() {
        return "<p>您的验证码为 <strong>${code}</strong>，有效期 ${expireMinutes} 分钟。</p>";
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