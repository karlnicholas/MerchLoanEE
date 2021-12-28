package com.github.karlnicholas.merchloan.ledger.config;

import com.github.karlnicholas.merchloan.jms.config.RabbitMqProperties;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    private final RabbitMqProperties rabbitMqProperties;
    @Value("${rabbitmq.ledger.debitaccount.queue}")
    private String ledgerDebitAccountQueue;
    @Value("${rabbitmq.ledger.creditaccount.queue}")
    private String ledgerCreditAccountQueue;
    @Value("${rabbitmq.ledger.query.loan.id.queue}")
    private String ledgerQueryLoanIdQueue;

    public RabbitMQConfig(RabbitMqProperties rabbitMqProperties) {
        this.rabbitMqProperties = rabbitMqProperties;
    }

    @Bean
    public Exchange exchange() {
        return ExchangeBuilder.directExchange(rabbitMqProperties.getExchange()).durable(false).build();
    }
    @Bean
    Queue ledgerDebitAccountQueue() {
        return new Queue(ledgerDebitAccountQueue, false);
    }
    @Bean
    Binding debitAccountBinding() {
        return BindingBuilder
                .bind(ledgerDebitAccountQueue())
                .to(exchange())
                .with(rabbitMqProperties.getLedgerDebitAccountRoutingkey())
                .noargs();
    }
    @Bean
    Queue ledgerCreditAccountQueue() {
        return new Queue(ledgerCreditAccountQueue, false);
    }
    @Bean
    Binding creditAccountBinding() {
        return BindingBuilder
                .bind(ledgerCreditAccountQueue())
                .to(exchange())
                .with(rabbitMqProperties.getLedgerCreditAccountRoutingkey())
                .noargs();
    }
    @Bean
    Queue ledgerQueryLoanIdQueue() {
        return new Queue(ledgerQueryLoanIdQueue, false);
    }
    @Bean
    Binding queryLoanIdBinding() {
        return BindingBuilder
                .bind(ledgerQueryLoanIdQueue())
                .to(exchange())
                .with(rabbitMqProperties.getLedgerQueryLoanIdRoutingkey())
                .noargs();
    }
}