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
    @Value("${rabbitmq.register.statementheader.queue}")
    private String registerStatementHeaderQueue;
    @Value("${rabbitmq.register.billingcyclecharge.queue}")
    private String registerBillingCycleChargeQueue;
    @Value("${rabbitmq.register.closeloan.queue}")
    private String registerCloseLoanQueue;
    @Value("${rabbitmq.register.query.loan.id.queue}")
    private String registerQueryLoanIdQueue;

    public RabbitMQConfig(RabbitMqProperties rabbitMqProperties) {
        this.rabbitMqProperties = rabbitMqProperties;
    }

    @Bean
    public Exchange exchange() {
        return ExchangeBuilder.directExchange(rabbitMqProperties.getExchange()).durable(false).build();
    }
}