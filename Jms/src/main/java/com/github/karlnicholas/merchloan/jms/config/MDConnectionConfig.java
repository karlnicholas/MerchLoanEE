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
        factory.setHost(host);
        factory.setPassword(password);
        factory.setUsername(username);
        factory.setPort(port);
        factory.setVirtualHost(virtualHost);
        log.info("MDConnectionConfig {} {} {} {} {}", host, port, password, username, virtualHost);
        return factory;
    }

    @Bean
    public Connection getConnection(ConnectionFactory connectionFactory) throws IOException, TimeoutException {
        return connectionFactory.newConnection();
    }
//    @Bean
//    public MessageConverter jsonMessageConverter() {
//        return new Jackson2JsonMessageConverter();
//    }
//    @Bean
//    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
//        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
////        rabbitTemplate.setMessageConverter(jsonMessageConverter());
//        return rabbitTemplate;
//    }

}