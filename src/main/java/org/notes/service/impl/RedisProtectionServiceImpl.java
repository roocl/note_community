package org.notes.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.notes.service.RedisProtectionService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import java.util.List;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisProtectionServiceImpl implements RedisProtectionService {

    private static final String NULL_MARKER_PREFIX = "cache:null:";
    private static final Duration NULL_MARKER_TTL = Duration.ofMinutes(5);
    private static final DefaultRedisScript<Long> UNLOCK = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean hasNullMarker(String domain, Object id) {
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(nullMarkerKey(domain, id)));
        } catch (Exception e) {
            log.warn("Redis null-marker check failed, domain={}, id={}", domain, id, e);
            return false;
        }
    }

    @Override
    public void setNullMarker(String domain, Object id) {
        try {
            stringRedisTemplate.opsForValue().set(
                    nullMarkerKey(domain, id),
                    "1",
                    withJitter(NULL_MARKER_TTL, 60));
        } catch (Exception e) {
            log.warn("Redis null-marker write failed, domain={}, id={}", domain, id, e);
        }
    }

    @Override
    public String tryLock(String lockKey, Duration ttl) {
        String token = UUID.randomUUID().toString();
        try {
            Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, token, ttl);
            return Boolean.TRUE.equals(locked) ? token : null;
        } catch (Exception e) {
            log.warn("Redis lock acquire failed, key={}", lockKey, e);
            return null;
        }
    }

    @Override
    public void unlock(String lockKey, String token) {
        if (token == null) {
            return;
        }
        try {
            stringRedisTemplate.execute(UNLOCK, List.of(lockKey), token);
        } catch (Exception e) {
            log.warn("Redis lock release failed, key={}", lockKey, e);
        }
    }

    @Override
    public Duration withJitter(Duration baseTtl, int jitterSeconds) {
        if (jitterSeconds <= 0) {
            return baseTtl;
        }
        int delta = ThreadLocalRandom.current().nextInt(jitterSeconds + 1);
        return baseTtl.plusSeconds(delta);
    }

    private String nullMarkerKey(String domain, Object id) {
        return NULL_MARKER_PREFIX + domain + ":" + id;
    }
}
