package com.saki.sakiaicodetoolsbackend.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.saki.sakiaicodetoolsbackend.constant.AuthConstants;
import com.saki.sakiaicodetoolsbackend.exception.BusinessException;
import com.saki.sakiaicodetoolsbackend.exception.ErrorCode;
import com.saki.sakiaicodetoolsbackend.exception.ThrowUtils;
import com.saki.sakiaicodetoolsbackend.mapper.UserMapper;
import com.saki.sakiaicodetoolsbackend.model.dto.LoginRequest;
import com.saki.sakiaicodetoolsbackend.model.dto.TokenRefreshRequest;
import com.saki.sakiaicodetoolsbackend.model.entity.User;
import com.saki.sakiaicodetoolsbackend.model.enums.LoginTypeEnum;
import com.saki.sakiaicodetoolsbackend.model.vo.UserVO;
import com.saki.sakiaicodetoolsbackend.service.UserService;
import com.saki.sakiaicodetoolsbackend.service.login.LoginStrategy;
import com.saki.sakiaicodetoolsbackend.service.login.LoginStrategyFactory;
import com.saki.sakiaicodetoolsbackend.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailException;
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
 * 用户表 服务层实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final String USER_TABLE = "saki_ai_code_tools.user";
    private static final String EMAIL_TEMPLATE_PATH = "templates/login-code.html";
    private static final String EMAIL_SUBJECT = "登录验证码";

    private final LoginStrategyFactory loginStrategyFactory;
    private final JwtUtils jwtUtils;
    private final StringRedisTemplate stringRedisTemplate;
    private final JavaMailSender mailSender;

    private final AtomicReference<String> emailTemplateCache = new AtomicReference<>();

    @Override
    public UserVO login(LoginRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_MISSING, "请求参数不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(request.getLoginType()), ErrorCode.PARAMS_MISSING, "登录类型不能为空");
        Optional<LoginTypeEnum> loginTypeOptional = LoginTypeEnum.fromValue(request.getLoginType());
        LoginTypeEnum loginType = loginTypeOptional.orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的登录方式"));
        LoginStrategy strategy = loginStrategyFactory.getStrategy(loginType);
        User user = strategy.authenticate(request);
        return buildLoginResult(user);
    }

    @Override
    public void sendEmailLoginCode(LoginRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_MISSING, "请求参数不能为空");
        String email = request.getUserEmail();
        ThrowUtils.throwIf(StrUtil.isBlank(email), ErrorCode.PARAMS_MISSING, "邮箱不能为空");
        User user = selectUserByColumn("user_email", email);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "邮箱未注册");
        String code = RandomUtil.randomNumbers(6);
        String redisKey = AuthConstants.buildEmailCodeKey(email);
        stringRedisTemplate.opsForValue().set(redisKey, code, Duration.ofMinutes(AuthConstants.EMAIL_CODE_EXPIRE_MINUTES));
        try {
            sendEmailCode(email, code);
        } catch (MessagingException | IOException | MailException ex) {
            stringRedisTemplate.delete(redisKey);
            log.error("发送登录验证码失败", ex);
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAILED, "发送验证码失败，请稍后重试");
        }
    }

    @Override
    public String refreshAccessToken(TokenRefreshRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_MISSING, "请求参数不能为空");
        String accessToken = request.getAccessToken();
        ThrowUtils.throwIf(StrUtil.isBlank(accessToken), ErrorCode.PARAMS_MISSING, "AccessToken 不能为空");
        Claims claims = jwtUtils.parseTokenAllowExpired(accessToken);
        String subject = claims.getSubject();
        ThrowUtils.throwIf(StrUtil.isBlank(subject), ErrorCode.TOKEN_INVALID, "无效的 AccessToken");
        Long userId;
        try {
            userId = Long.valueOf(subject);
        } catch (NumberFormatException ex) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID, "非法的用户标识");
        }
        String refreshTokenKey = AuthConstants.buildRefreshTokenKey(userId);
        String refreshToken = stringRedisTemplate.opsForValue().get(refreshTokenKey);
        ThrowUtils.throwIf(StrUtil.isBlank(refreshToken), ErrorCode.LOGIN_EXPIRED, "登录状态已过期，请重新登录");
        if (!jwtUtils.isTokenValid(refreshToken)) {
            stringRedisTemplate.delete(refreshTokenKey);
            throw new BusinessException(ErrorCode.LOGIN_EXPIRED, "登录状态已过期，请重新登录");
        }
        String userRole = claims.get("userRole", String.class);
        if (StrUtil.isBlank(userRole)) {
            User user = getById(userId);
            ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
            userRole = StrUtil.blankToDefault(user.getUserRole(), "user");
        }
        String newAccessToken = jwtUtils.generateAccessToken(userId, userRole);
        String newRefreshToken = jwtUtils.generateRefreshToken(userId, userRole);
        storeRefreshToken(userId, newRefreshToken);
        return newAccessToken;
    }

    private UserVO buildLoginResult(User user) {
        ThrowUtils.throwIf(user == null, ErrorCode.SYSTEM_ERROR, "用户信息为空");
        String userRole = StrUtil.blankToDefault(user.getUserRole(), "user");
        String accessToken = jwtUtils.generateAccessToken(user.getId(), userRole);
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), userRole);
        storeRefreshToken(user.getId(), refreshToken);
        updateLastLoginInfo(user);
        return convertToUserVO(user, accessToken);
    }

    private void storeRefreshToken(Long userId, String refreshToken) {
        String key = AuthConstants.buildRefreshTokenKey(userId);
        stringRedisTemplate.opsForValue().set(key, refreshToken, jwtUtils.getRefreshTokenExpireDuration());
    }

    private void updateLastLoginInfo(User user) {
        LocalDateTime now = LocalDateTime.now();
        User update = new User();
        update.setId(user.getId());
        update.setLastLoginTime(now);
        boolean updated = this.updateById(update);
        if (!updated) {
            log.warn("更新用户最后登录时间失败，用户ID={}", user.getId());
        } else {
            user.setLastLoginTime(now);
        }
    }

    private UserVO convertToUserVO(User user, String accessToken) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUserAccount(user.getUserAccount());
        vo.setUserEmail(user.getUserEmail());
        vo.setUserPhone(user.getUserPhone());
        vo.setUserAvatar(user.getUserAvatar());
        vo.setUserProfile(user.getUserProfile());
        vo.setUserRole(user.getUserRole());
        vo.setUserStatus(user.getUserStatus());
        vo.setIsVip(user.getIsVip());
        vo.setVipStartTime(user.getVipStartTime());
        vo.setVipEndTime(user.getVipEndTime());
        vo.setInviteCode(user.getInviteCode());
        vo.setLastLoginTime(user.getLastLoginTime());
        vo.setLastLoginIp(user.getLastLoginIp());
        vo.setEditTime(user.getEditTime());
        vo.setCreateTime(user.getCreateTime());
        vo.setAccessToken(accessToken);
        return vo;
    }

    private void sendEmailCode(String email, String code) throws MessagingException, IOException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, StandardCharsets.UTF_8.name());
        helper.setTo(email);
        helper.setSubject(EMAIL_SUBJECT);
        helper.setText(buildEmailContent(code), true);
        mailSender.send(mimeMessage);
    }

    private String buildEmailContent(String code) throws IOException {
        String template = emailTemplateCache.get();
        if (StrUtil.isBlank(template)) {
            template = loadTemplate();
            emailTemplateCache.set(template);
        }
        if (StrUtil.isBlank(template)) {
            template = defaultTemplate();
        }
        return template.replace("${code}", code)
                .replace("${expireMinutes}", String.valueOf(AuthConstants.EMAIL_CODE_EXPIRE_MINUTES));
    }

    private String loadTemplate() throws IOException {
        ClassPathResource resource = new ClassPathResource(EMAIL_TEMPLATE_PATH);
        if (!resource.exists()) {
            return defaultTemplate();
        }
        try {
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            log.error("读取邮件模板失败", ex);
            throw ex;
        }
    }

    private String defaultTemplate() {
        return "<p>您的验证码为 <strong>${code}</strong>，有效期 ${expireMinutes} 分钟。</p>";
    }

    private User selectUserByColumn(String column, String value) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(USER_TABLE)
                .where(column + " = ?", value)
                .limit(1);
        return getBaseMapper().selectOneByQuery(queryWrapper);
    }
}

