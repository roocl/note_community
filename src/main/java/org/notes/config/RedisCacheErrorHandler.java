package org.notes.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

@Slf4j
public class RedisCacheErrorHandler implements CacheErrorHandler {

    @Override
    public void handleCacheGetError(RuntimeException e, Cache cache, Object key) {
        log.warn("Redis GET 失败，降级查 MySQL。cache={}, key={}", cache.getName(), key, e);
    }

    @Override
    public void handleCachePutError(RuntimeException e, Cache cache, Object key, Object value) {
        log.warn("Redis PUT 失败，跳过缓存写入。cache={}, key={}", cache.getName(), key, e);
    }

    @Override
    public void handleCacheEvictError(RuntimeException e, Cache cache, Object key) {
        log.warn("Redis EVICT 失败，跳过缓存清除。cache={}, key={}", cache.getName(), key, e);
    }

    @Override
    public void handleCacheClearError(RuntimeException e, Cache cache) {
        log.warn("Redis CLEAR 失败。cache={}", cache.getName(), e);
    }
}
