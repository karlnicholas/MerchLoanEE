package com.github.karlnicholas.merchloan.redis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDate;

@Configuration
public class RedisConfig {

    private @Value("${redis.host:localhost}") String redisHost;
    private @Value("${redis.port:6379}") int redisPort;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
    @Bean
    public LettuceConnectionFactory connectionFactory() {

        RedisStandaloneConfiguration redisConf = new RedisStandaloneConfiguration();
        redisConf.setHostName(redisHost);
        redisConf.setPort(redisPort);

        return new LettuceConnectionFactory(redisConf);
    }

    @Bean
    RedisTemplate<?, ?> redisTemplate() {
        RedisTemplate<Long, LocalDate> template = new RedisTemplate<>();
        template.setConnectionFactory( connectionFactory() );
        return template;
    }
}
