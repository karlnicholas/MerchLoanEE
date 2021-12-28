package com.github.karlnicholas.merchloan.servicerequest.config;

import com.github.karlnicholas.merchloan.jms.config.RabbitMqProperties;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    private final RabbitMqProperties rabbitMqProperties;
    @Value("${rabbitmq.servicerequest.queue}")
    private String servicerequestQueue;
    @Value("${rabbitmq.servicerequest.query.id.queue}")
    private String servicerequestQueryIdQueue;

    public RabbitMQConfig(RabbitMqProperties rabbitMqProperties) {
        this.rabbitMqProperties = rabbitMqProperties;
    }

    @Bean
    Exchange exchange() {
        return ExchangeBuilder.directExchange(rabbitMqProperties.getExchange()).durable(false).build();
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
                .with(rabbitMqProperties.getServicerequestRoutingkey())
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
                .with(rabbitMqProperties.getServicerequestQueryIdRoutingkey())
                .noargs();
    }
}