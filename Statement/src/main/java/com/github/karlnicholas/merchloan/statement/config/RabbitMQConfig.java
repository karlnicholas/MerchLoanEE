package com.github.karlnicholas.merchloan.statement.config;

import com.github.karlnicholas.merchloan.jms.config.RabbitMqProperties;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    private final RabbitMqProperties rabbitMqProperties;
    @Value("${rabbitmq.statement.statement.queue}")
    private String statementStatementQueue;
    @Value("${rabbitmq.statement.query.statement.queue}")
    private String statementQueryStatementQueue;

    public RabbitMQConfig(RabbitMqProperties rabbitMqProperties) {
        this.rabbitMqProperties = rabbitMqProperties;
    }

    @Bean
    Exchange exchange() {
        return ExchangeBuilder.directExchange(rabbitMqProperties.getExchange()).durable(false).build();
    }
    @Bean
    Queue statementQueue() {
        return new Queue(statementStatementQueue, false);
    }
    @Bean
    Binding statementBinding() {
        return BindingBuilder
                .bind(statementQueue())
                .to(exchange())
                .with(rabbitMqProperties.getStatementStatementRoutingkey())
                .noargs();
    }
    @Bean
    Queue statementQueryStatementQueue() {
        return new Queue(statementQueryStatementQueue, false);
    }
    @Bean
    Binding statementQueryStatementBinding() {
        return BindingBuilder
                .bind(statementQueryStatementQueue())
                .to(exchange())
                .with(rabbitMqProperties.getStatementQueryStatementRoutingkey())
                .noargs();
    }
}