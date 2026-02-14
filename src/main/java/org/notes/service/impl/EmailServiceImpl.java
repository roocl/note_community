package org.notes.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.notes.config.RabbitMQConfig;
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
        // 检查发送频率
        if (isVerificationCodeRateLimited(email)) {
            throw new RuntimeException("验证码发送太频繁，请60秒后重试");
        }

        // 生成6位随机验证码
        String verificationCode = RandomCodeUtil.generateNumberCode(6);

        // 实现异步发送邮件的逻辑
        try {
            // 构造对象
            EmailTask emailTask = new EmailTask();
            emailTask.setEmail(email);
            emailTask.setCode(verificationCode);
            emailTask.setTimestamp(System.currentTimeMillis());

            // 发送消息到rabbitmq
            rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_QUEUE, emailTask);

            // 将验证码存入redis，供注册时校验
            String codeKey = RedisKey.registerVerificationCode(email);
            redisTemplate.opsForValue().set(codeKey, verificationCode, expireMinutes, TimeUnit.MINUTES);

            // 设置发送频率限制标记
            String limitKey = RedisKey.registerVerificationLimitCode(email);
            redisTemplate.opsForValue().set(limitKey, "1", limitExpireSeconds, TimeUnit.SECONDS);

            log.info("验证码邮件任务已发送到RabbitMQ，邮箱：{}", email);
        } catch (Exception e) {
            log.error("发送验证码邮件失败", e);
            throw new RuntimeException("发送验证码失败，请稍后重试");
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

    @Override
    public boolean isVerificationCodeRateLimited(String email) {
        String redisKey = RedisKey.registerVerificationLimitCode(email);
        return redisTemplate.opsForValue().get(redisKey) != null;
    }
}
