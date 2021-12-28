package com.github.karlnicholas.merchloan.ledger.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Value("${rabbitmq.exchange}")
    private String exchange;
    @Value("${rabbitmq.ledger.debitfromloan.routingkey}")
    private String ledgerDebitFromLoanRoutingKey;
    @Value("${rabbitmq.ledger.debitfromloan.queue}")
    private String ledgerDebitFromLoanQueue;
    @Value("${rabbitmq.ledger.credittoloan.routingkey}")
    private String ledgerCreditToLoanRoutingKey;
    @Value("${rabbitmq.ledger.credittoloan.queue}")
    private String ledgerCreditToLoanQueue;

    @Bean
    public Exchange exchange() {
        return ExchangeBuilder.directExchange(exchange).durable(false).build();
    }
    @Bean
    Queue ledgerDebitFromLoanQueue() {
        return new Queue(ledgerDebitFromLoanQueue, false);
    }
    @Bean
    Binding DebitFromLoanBinding() {
        return BindingBuilder
                .bind(ledgerDebitFromLoanQueue())
                .to(exchange())
                .with(ledgerDebitFromLoanRoutingKey)
                .noargs();
    }
    @Bean
    Queue ledgerCreditToLoanQueue() {
        return new Queue(ledgerCreditToLoanQueue, false);
    }
    @Bean
    Binding CreditToLoanBinding() {
        return BindingBuilder
                .bind(ledgerCreditToLoanQueue())
                .to(exchange())
                .with(ledgerCreditToLoanRoutingKey)
                .noargs();
    }
}