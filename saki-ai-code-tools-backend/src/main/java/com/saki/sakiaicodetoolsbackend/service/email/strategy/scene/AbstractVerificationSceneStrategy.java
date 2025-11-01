package com.saki.sakiaicodetoolsbackend.service.email.strategy.scene;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.saki.sakiaicodetoolsbackend.constant.AuthConstants;
import com.saki.sakiaicodetoolsbackend.exception.BusinessException;
import com.saki.sakiaicodetoolsbackend.exception.ErrorCode;
import com.saki.sakiaicodetoolsbackend.exception.ThrowUtils;
import com.saki.sakiaicodetoolsbackend.mapper.UserMapper;
import com.saki.sakiaicodetoolsbackend.model.dto.common.SendCodeRequest;
import com.saki.sakiaicodetoolsbackend.model.entity.User;
import com.saki.sakiaicodetoolsbackend.model.enums.MailTemplateEnum;
import com.saki.sakiaicodetoolsbackend.model.enums.VerificationSceneEnum;
import com.saki.sakiaicodetoolsbackend.service.email.strategy.channel.VerificationChannelFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Map;

/**
 * 验证码发送场景抽象模板
 * 定义了验证码发送的标准流程（模板方法模式）
 * 子类只需实现特定业务校验逻辑
 *
 * @author saki
 * @version 1.0
 * @since 2025-10-24
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractVerificationSceneStrategy implements VerificationSceneStrategy {

    protected final StringRedisTemplate stringRedisTemplate;
    /**
     * 验证渠道工厂
     */
    protected final VerificationChannelFactory channelFactory;
    protected final UserMapper userMapper;

    /**
     * 执行验证码发送流程（模板方法）
     * 包含参数校验、前置检查、验证码生成、存储和发送的完整流程
     *
     * @param request 验证码发送请求
     * @throws BusinessException 当参数校验失败、前置检查不通过或发送失败时抛出
     */
    @Override
    public void execute(SendCodeRequest request) {
        String receiver = StrUtil.trimToNull(request.getReceiver());
        String channel = request.getChannel();

        ThrowUtils.throwIf(StrUtil.isBlank(receiver), ErrorCode.PARAMS_MISSING, "接收者不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(channel), ErrorCode.PARAMS_MISSING, "发送渠道不能为空");

        preCheck(receiver, channel);

        String code = RandomUtil.randomNumbers(6);
        String redisKey = AuthConstants.buildEmailCodeKey(getScene(), receiver);


        Long expire = stringRedisTemplate.getExpire(redisKey);
        if (expire != null && expire > (AuthConstants.EMAIL_CODE_EXPIRE_MINUTES * 60 - 60)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "请勿频繁发送验证码");
        }

        stringRedisTemplate.opsForValue().set(redisKey, code, Duration.ofMinutes(AuthConstants.EMAIL_CODE_EXPIRE_MINUTES));

        Map<String, Object> variables = buildVariables(code, request);

        try {
            sendCode(receiver, channel, variables);
            log.info("验证码 [{}] 已发送至 {} via {}", code, receiver, channel);
        } catch (Exception e) {
            stringRedisTemplate.delete(redisKey);
            log.error("验证码发送失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAILED, "发送验证码失败，请稍后重试");
        }
    }

    /**
     * 根据渠道与接收者信息查询用户
     *
     * @param receiver 接收者（邮箱或手机号）
     * @param channel 渠道类型
     * @return 用户信息，如果不存在则返回null
     * @throws BusinessException 当渠道类型未知时抛出
     */
    protected User selectByReceiver(String receiver, String channel) {
        QueryWrapper queryWrapper = new QueryWrapper();

        if ("EMAIL".equalsIgnoreCase(channel)) {
            queryWrapper.eq(User::getUserEmail, receiver);
        } else if ("PHONE".equalsIgnoreCase(channel)) {
            queryWrapper.eq(User::getUserPhone, receiver);
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未知渠道类型");
        }

        queryWrapper.limit(1);
        return userMapper.selectOneByQuery(queryWrapper);
    }

    /**
     * 构建模板变量
     * 由子类实现具体的变量构建逻辑
     *
     * @param code 验证码
     * @param request 发送请求
     * @return 模板变量映射表
     */
    protected abstract Map<String, Object> buildVariables(String code, SendCodeRequest request);

    /**
     * 前置业务校验
     * 由子类实现特定场景的业务校验逻辑
     *
     * @param receiver 接收者
     * @param channel 渠道类型
     * @throws BusinessException 当校验不通过时抛出
     */
    protected abstract void preCheck(String receiver, String channel);

    /**
     * 获取邮件模板类型
     * 不同场景对应不同的邮件模板
     *
     * @return 邮件模板枚举
     */
    protected abstract MailTemplateEnum getTemplateType();

    /**
     * 执行验证码发送
     * 可被子类覆盖以实现自定义发送逻辑
     *
     * @param receiver 接收者
     * @param channel 渠道类型
     * @param variables 模板变量
     */
    protected void sendCode(String receiver, String channel, Map<String, Object> variables) {
        channelFactory.getStrategy(channel).send(receiver, getTemplateType(), variables);
    }

    /**
     * 每个业务场景对应的 Redis Key 前缀枚举
     */
    protected abstract VerificationSceneEnum getScene();
}
