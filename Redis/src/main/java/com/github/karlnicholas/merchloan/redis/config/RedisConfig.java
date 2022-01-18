package com.github.karlnicholas.merchloan.redis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDate;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<Long, LocalDate> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<Long, LocalDate> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        // Add some specific configuration here. Key serializers, etc.
        return template;
    }
}
