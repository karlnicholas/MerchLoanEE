package com.github.karlnicholas.merchloan.register.config;

import com.github.karlnicholas.merchloan.jms.config.RabbitMqProperties;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    private final RabbitMqProperties rabbitMqProperties;
    @Value("${rabbitmq.register.debitloan.queue}")
    private String registerDebitLoanQueue;
    @Value("${rabbitmq.register.creditloan.queue}")
    private String registerCreditLoanQueue;
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
    Queue registerDebitLoanQueue() {
        return new Queue(registerDebitLoanQueue, false);
    }
    @Bean
    Binding debitLoanBinding() {
        return BindingBuilder
                .bind(registerDebitLoanQueue())
                .to(exchange())
                .with(rabbitMqProperties.getRegisterDebitLoanRoutingkey())
                .noargs();
    }
    @Bean
    Queue registerCreditLoanQueue() {
        return new Queue(registerCreditLoanQueue, false);
    }
    @Bean
    Binding creditLoanBinding() {
        return BindingBuilder
                .bind(registerCreditLoanQueue())
                .to(exchange())
                .with(rabbitMqProperties.getRegisterCreditLoanRoutingkey())
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