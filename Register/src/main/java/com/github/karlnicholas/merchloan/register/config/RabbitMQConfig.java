package com.github.karlnicholas.merchloan.register.config;

import com.github.karlnicholas.merchloan.jms.config.RabbitMqProperties;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    private final RabbitMqProperties rabbitMqProperties;
    @Value("${rabbitmq.register.fundloan.queue}")
    private String registerFundLoanQueue;
    @Value("${rabbitmq.register.creditloan.queue}")
    private String registerCreditLoanQueue;
    @Value("${rabbitmq.register.debitloan.queue}")
    private String registerDebitLoanQueue;
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
    Queue fundLoanQueue() {
        return new Queue(registerFundLoanQueue, false);
    }
    @Bean
    Binding fundLoanBinding() {
        return BindingBuilder
                .bind(fundLoanQueue())
                .to(exchange())
                .with(rabbitMqProperties.getRegisterFundLoanRoutingkey())
                .noargs();
    }
    @Bean
    Queue creditLoanQueue() {
        return new Queue(registerCreditLoanQueue, false);
    }
    @Bean
    Binding creditLoanBinding() {
        return BindingBuilder
                .bind(creditLoanQueue())
                .to(exchange())
                .with(rabbitMqProperties.getRegisterCreditLoanRoutingkey())
                .noargs();
    }
    @Bean
    Queue debitLoanQueue() {
        return new Queue(registerDebitLoanQueue, false);
    }
    @Bean
    Binding debitLoanBinding() {
        return BindingBuilder
                .bind(debitLoanQueue())
                .to(exchange())
                .with(rabbitMqProperties.getRegisterDebitLoanRoutingkey())
                .noargs();
    }
    @Bean
    Queue queryLoanIdQueue() {
        return new Queue(registerQueryLoanIdQueue, false);
    }
    @Bean
    Binding queryLoanIdBinding() {
        return BindingBuilder
                .bind(queryLoanIdQueue())
                .to(exchange())
                .with(rabbitMqProperties.getRegisterQueryLoanIdRoutingkey())
                .noargs();
    }
}