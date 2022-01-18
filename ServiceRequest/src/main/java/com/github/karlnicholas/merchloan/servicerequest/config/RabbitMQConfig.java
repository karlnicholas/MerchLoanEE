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
    @Value("${rabbitmq.servicerequest.checkrequest.queue}")
    private String servicerequestCheckRequestQueue;
    @Value("${rabbitmq.servicerequest.billloan.queue}")
    private String servicerequestBillloanQueue;

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
    @Bean
    public Queue servicerequestCheckRequestsQueue() {
        return new Queue(servicerequestCheckRequestQueue, false);
    }
    @Bean
    public Binding servicerequestCheckRequestsBinding() {
        return BindingBuilder
                .bind(servicerequestCheckRequestsQueue())
                .to(exchange())
                .with(rabbitMqProperties.getServicerequestCheckRequestRoutingkey())
                .noargs();
    }
    @Bean
    Queue servicerequestBillloanQueue() {
        return new Queue(servicerequestBillloanQueue, false);
    }
    @Bean
    Binding servicerequestBillloanBinding() {
        return BindingBuilder
                .bind(servicerequestBillloanQueue())
                .to(exchange())
                .with(rabbitMqProperties.getServicerequestBillLoanRoutingkey())
                .noargs();
    }
}