package org.notes.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisProtectionServiceImplTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RedisProtectionServiceImpl redisProtectionService;

    @Test
    void nullMarkerUsesDomainScopedKey() {
        when(stringRedisTemplate.hasKey("cache:null:user:9")).thenReturn(true);

        assertTrue(redisProtectionService.hasNullMarker("user", 9L));
    }

    @Test
    void tryLockReturnsTokenWhenRedisLockAcquired() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(eq("lock:key"), anyString(), eq(Duration.ofSeconds(5)))).thenReturn(true);

        String token = redisProtectionService.tryLock("lock:key", Duration.ofSeconds(5));

        assertNotNull(token);
    }

    @Test
    void unlockDeletesOnlyWhenTokenMatches() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("lock:key")).thenReturn("token");

        redisProtectionService.unlock("lock:key", "token");

        verify(stringRedisTemplate).delete("lock:key");
    }

    @Test
    void withJitterKeepsTtlWithinConfiguredRange() {
        Duration ttl = redisProtectionService.withJitter(Duration.ofMinutes(30), 300);

        assertFalse(ttl.minus(Duration.ofMinutes(30)).isNegative());
        assertTrue(ttl.compareTo(Duration.ofMinutes(35)) <= 0);
    }
}
