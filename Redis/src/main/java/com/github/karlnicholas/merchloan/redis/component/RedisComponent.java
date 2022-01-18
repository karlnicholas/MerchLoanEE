package com.github.karlnicholas.merchloan.redis.component;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class RedisComponent {
    private final RedisTemplate<Long, LocalDate> redisTemplate;

    public RedisComponent(RedisTemplate<Long, LocalDate> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void updateBusinessDate(LocalDate businessDate) {
        redisTemplate.opsForValue().set(1L, businessDate);
    }

    public LocalDate getBusinessDate() {
        return redisTemplate.opsForValue().get(1L);
    }
}
