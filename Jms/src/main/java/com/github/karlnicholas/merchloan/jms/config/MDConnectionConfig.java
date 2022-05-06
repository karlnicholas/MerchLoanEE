package com.github.karlnicholas.merchloan.jms.config;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Configuration
@Slf4j
public class MDConnectionConfig {
    @Value("${rabbitmq.username:guest}")
    private String username;
    @Value("${rabbitmq.password:guest}")
    private String password;
    @Value("${rabbitmq.host:localhost}")
    private String host;
    @Value("${rabbitmq.port:5672}")
    private Integer port;
    @Value("${rabbitmq.virtual-host:/}")
    private String virtualHost;

    @Bean
    public ConnectionFactory getConnectionFactory() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setPassword(password);
        factory.setUsername(username);
        factory.setVirtualHost(virtualHost);
        factory.setHost(host);
        factory.setPort(port);
        return factory;
    }

    @Bean
    public Connection getConnection(ConnectionFactory connectionFactory) throws IOException, TimeoutException, InterruptedException {
        int retryCount = 0;
        while (retryCount < 3) {
            try {
                return connectionFactory.newConnection();
            } catch (java.net.ConnectException e) {
                Thread.sleep(5000);
                // apply retry logic
                retryCount++;
                if (retryCount >= 3) {
                    throw e;
                }
            }
        }
        return null;
    }

}