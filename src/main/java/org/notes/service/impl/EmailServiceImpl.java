package org.notes.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.notes.config.RabbitMQConfig;
import org.notes.exception.BadRequestException;
import org.notes.exception.BaseException;
import org.notes.model.enums.redisKey.RedisKey;
import org.notes.service.EmailService;
import org.notes.task.email.EmailTask;
import org.notes.utils.RandomCodeUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Value("${mail.verify-code.expire-minutes}")
    private int expireMinutes;

    @Value("${mail.verify-code.limit-expire-seconds}")
    private int limitExpireSeconds;

    @Override
    public void sendVerificationCode(String email) {
        // 原子性检查并设置发送频率限制（Redis 异常时降级放行）
        String limitKey = RedisKey.registerVerificationLimitCode(email);
        Boolean acquired = null;
        try {
            acquired = redisTemplate.opsForValue()
                    .setIfAbsent(limitKey, "1", limitExpireSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Redis 频率限制检查异常，降级放行。email={}", email, e);
        }
        if (Boolean.FALSE.equals(acquired)) {
            throw new BadRequestException("验证码发送过于频繁，请" + limitExpireSeconds + "秒后再试");
        }

        // 生成6位随机验证码
        String verificationCode = RandomCodeUtil.generateNumberCode(6);

        try {
            EmailTask emailTask = new EmailTask();
            emailTask.setEmail(email);
            emailTask.setCode(verificationCode);
            emailTask.setTimestamp(System.currentTimeMillis());

            rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_QUEUE, emailTask);

            String codeKey = RedisKey.registerVerificationCode(email);
            try {
                redisTemplate.opsForValue().set(codeKey, verificationCode, expireMinutes, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.warn("Redis 验证码存储异常，跳过缓存写入。email={}", email, e);
            }

            log.info("验证码邮件任务已发送到RabbitMQ，邮箱：{}", email);
        } catch (Exception e) {
            // 发送失败，清除限流标记允许重试
            try {
                redisTemplate.delete(limitKey);
            } catch (Exception ex) {
                log.warn("Redis 清除限流标记异常。key={}", limitKey, ex);
            }
            log.error("发送验证码邮件失败", e);
            throw new BaseException("发送验证码失败，请稍后重试", e);
        }
    }

    @Override
    public boolean checkVerificationCode(String email, String code) {
        String redisKey = RedisKey.registerVerificationCode(email);
        String verificationCode = redisTemplate.opsForValue().get(redisKey);

        if (verificationCode != null && Objects.equals(verificationCode, code)) {
            redisTemplate.delete(redisKey);
            return true;
        }
        return false;
    }
}
