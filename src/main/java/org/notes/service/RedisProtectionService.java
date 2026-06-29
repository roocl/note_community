package org.notes.service;

import java.time.Duration;

public interface RedisProtectionService {

    boolean hasNullMarker(String domain, Object id);

    void setNullMarker(String domain, Object id);

    String tryLock(String lockKey, Duration ttl);

    void unlock(String lockKey, String token);

    Duration withJitter(Duration baseTtl, int jitterSeconds);
}
