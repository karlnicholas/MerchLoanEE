package com.github.karlnicholas.merchloan.jms.config;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Configuration
public class MDConnectionConfig {
    @Value("${rabbitmq.username}")
    private String username;
    @Value("${rabbitmq.password}")
    private String password;
    @Value("${rabbitmq.host}")
    private String host;
    @Value("${rabbitmq.port}")
    private Integer port;
    @Value("${rabbitmq.virtual-host}")
    private String virtualHost;

    @Bean
    public ConnectionFactory getConnectionFactory() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPassword(password);
        factory.setUsername(username);
        factory.setPort(port);
        factory.setVirtualHost(virtualHost);
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