package com.github.karlnicholas.merchloan.redis.component;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class RedisComponent {
    private final RedisTemplate<Long, LocalDate> redisTemplateBusinessDate;

    public RedisComponent(RedisTemplate<Long, LocalDate> redisTemplateBusinessDate) {
        this.redisTemplateBusinessDate = redisTemplateBusinessDate;
    }

    public void updateBusinessDate(LocalDate businessDate) {
        redisTemplateBusinessDate.opsForValue().set(1L, businessDate);
    }

    public LocalDate getBusinessDate() {
        return redisTemplateBusinessDate.opsForValue().get(1L);
    }

}
