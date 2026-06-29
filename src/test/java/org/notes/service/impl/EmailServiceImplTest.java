package org.notes.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.notes.model.enums.redisKey.RedisKey;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailServiceImpl 单元测试")
class EmailServiceImplTest {

    @Mock
    private RabbitTemplate rabbitTemplate;
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "expireMinutes", 15);
        ReflectionTestUtils.setField(emailService, "limitExpireSeconds", 60);
    }

    // ==================== sendVerificationCode ====================

    @Test
    @DisplayName("sendVerificationCode - 成功：发送到MQ并写入Redis")
    void sendVerificationCode_success() {
        String email = "test@example.com";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(
                eq(RedisKey.registerVerificationLimitCode(email)),
                eq("1"),
                eq(60L),
                eq(TimeUnit.SECONDS)
        )).thenReturn(true);

        emailService.sendVerificationCode(email);

        // 验证发送到 RabbitMQ
        verify(rabbitTemplate).convertAndSend(anyString(), any(Object.class));
        // 验证验证码写入 Redis
        verify(valueOperations).set(eq(RedisKey.registerVerificationCode(email)), anyString(), eq(15L), any());
        // 验证频率限制标记写入 Redis
        verify(valueOperations).setIfAbsent(
                eq(RedisKey.registerVerificationLimitCode(email)),
                eq("1"),
                eq(60L),
                eq(TimeUnit.SECONDS)
        );
    }

    @Test
    @DisplayName("sendVerificationCode - 频率限制")
    void sendVerificationCode_rateLimited() {
        String email = "test@example.com";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(
                eq(RedisKey.registerVerificationLimitCode(email)),
                eq("1"),
                eq(60L),
                eq(TimeUnit.SECONDS)
        )).thenReturn(false);

        assertThrows(RuntimeException.class, () -> emailService.sendVerificationCode(email));
        verify(rabbitTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    // ==================== checkVerificationCode ====================

    @Test
    @DisplayName("checkVerificationCode - 验证成功")
    void checkVerificationCode_success() {
        String email = "test@example.com";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(RedisKey.registerVerificationCode(email))).thenReturn("123456");

        assertTrue(emailService.checkVerificationCode(email, "123456"));
        verify(redisTemplate).delete(RedisKey.registerVerificationCode(email));
    }

    @Test
    @DisplayName("checkVerificationCode - 验证失败(错误验证码)")
    void checkVerificationCode_wrongCode() {
        String email = "test@example.com";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(RedisKey.registerVerificationCode(email))).thenReturn("123456");

        assertFalse(emailService.checkVerificationCode(email, "000000"));
    }

    @Test
    @DisplayName("checkVerificationCode - 验证失败(验证码不存在)")
    void checkVerificationCode_codeNotFound() {
        String email = "test@example.com";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(RedisKey.registerVerificationCode(email))).thenReturn(null);

        assertFalse(emailService.checkVerificationCode(email, "123456"));
    }
}
