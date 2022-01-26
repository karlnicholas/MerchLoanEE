package com.github.karlnicholas.merchloan.statement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

import static org.springframework.data.redis.core.RedisKeyValueAdapter.EnableKeyspaceEvents.ON_STARTUP;

@SpringBootApplication(scanBasePackages = {"com.github.karlnicholas.merchloan"})
public class StatementApplication {

    public static void main(String[] args) {
        SpringApplication.run(StatementApplication.class, args);
    }

}
