package com.github.karlnicholas.merchloan.servicerequest.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Value("${rabbitmq.servicerequest.queue}")
    private String servicerequestQueue;
    @Value("${rabbitmq.servicerequest.routingkey}")
    private String servicerequestRoutingKey;
    @Value("${rabbitmq.servicerequest.query.id.queue}")
    private String servicerequestQueryIdQueue;
    @Value("${rabbitmq.servicerequest.query.id.routingkey}")
    private String servicerequestQueryIdRoutingkey;
    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Bean
    Exchange exchange() {
        return ExchangeBuilder.directExchange(exchange).durable(false).build();
    }
    @Bean
    Queue servicerequestQueue() {
        return new Queue(servicerequestQueue, false);
    }
    @Bean
    Binding servicerequestBinding() {
        return BindingBuilder
                .bind(servicerequestQueue())
                .to(exchange())
                .with(servicerequestRoutingKey)
                .noargs();
    }
    @Bean
    Queue servicerequestQueryIdQueue() {
        return new Queue(servicerequestQueryIdQueue, false);
    }
    @Bean
    Binding servicerequestQueryIdBinding() {
        return BindingBuilder
                .bind(servicerequestQueryIdQueue())
                .to(exchange())
                .with(servicerequestQueryIdRoutingkey)
                .noargs();
    }
}