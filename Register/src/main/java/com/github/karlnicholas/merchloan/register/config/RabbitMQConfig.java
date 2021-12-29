package com.github.karlnicholas.merchloan.register.config;

import com.github.karlnicholas.merchloan.jms.config.RabbitMqProperties;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    private final RabbitMqProperties rabbitMqProperties;
    @Value("${rabbitmq.register.debitaccount.queue}")
    private String registerDebitAccountQueue;
    @Value("${rabbitmq.register.creditaccount.queue}")
    private String registerCreditAccountQueue;
    @Value("${rabbitmq.register.query.loan.id.queue}")
    private String registerQueryLoanIdQueue;

    public RabbitMQConfig(RabbitMqProperties rabbitMqProperties) {
        this.rabbitMqProperties = rabbitMqProperties;
    }

    @Bean
    public Exchange exchange() {
        return ExchangeBuilder.directExchange(rabbitMqProperties.getExchange()).durable(false).build();
    }
    @Bean
    Queue registerDebitAccountQueue() {
        return new Queue(registerDebitAccountQueue, false);
    }
    @Bean
    Binding debitAccountBinding() {
        return BindingBuilder
                .bind(registerDebitAccountQueue())
                .to(exchange())
                .with(rabbitMqProperties.getRegisterDebitAccountRoutingkey())
                .noargs();
    }
    @Bean
    Queue registerCreditAccountQueue() {
        return new Queue(registerCreditAccountQueue, false);
    }
    @Bean
    Binding creditAccountBinding() {
        return BindingBuilder
                .bind(registerCreditAccountQueue())
                .to(exchange())
                .with(rabbitMqProperties.getRegisterCreditAccountRoutingkey())
                .noargs();
    }
    @Bean
    Queue registerQueryLoanIdQueue() {
        return new Queue(registerQueryLoanIdQueue, false);
    }
    @Bean
    Binding queryLoanIdBinding() {
        return BindingBuilder
                .bind(registerQueryLoanIdQueue())
                .to(exchange())
                .with(rabbitMqProperties.getRegisterQueryLoanIdRoutingkey())
                .noargs();
    }
}