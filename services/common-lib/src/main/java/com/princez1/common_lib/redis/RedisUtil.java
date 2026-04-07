package com.princez1.common_lib.redis;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    public void set(String key, Object value, long ttl, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, ttl, unit);
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public boolean setIfAbsent(String key, Object value, long ttl, TimeUnit unit) {
        Boolean ok = redisTemplate.opsForValue().setIfAbsent(key, value, ttl, unit);
        return Boolean.TRUE.equals(ok);
    }

    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    public boolean expire(String key, long ttl, TimeUnit unit) {
        return redisTemplate.expire(key, ttl, unit);
    }
}
