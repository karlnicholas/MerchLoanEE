package com.github.karlnicholas.merchloan.accounts.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Value("${rabbitmq.account.createaccount.queue}")
    private String accountCreateAccountQueue;
    @Value("${rabbitmq.account.createaccount.routingkey}")
    private String accountCreateAccountRoutingkey;
    @Value("${rabbitmq.account.funding.queue}")
    private String accountFundingQueue;
    @Value("${rabbitmq.account.funding.routingkey}")
    private String accountFundingRoutingkey;
    @Value("${rabbitmq.exchange}")
    private String exchange;
    @Value("${rabbitmq.username}")
    private String username;
    @Value("${rabbitmq.password}")
    private String password;
    @Value("${rabbitmq.host}")
    private String host;
    @Value("${rabbitmq.port}")
    private Integer port;
    @Value("${rabbitmq.virtual-host}")
    private String virtualHost;
    @Bean
    Exchange exchange() {
        return ExchangeBuilder.directExchange(exchange).durable(false).build();
    }
    @Bean
    Queue accountCreateAccountQueue() {
        return new Queue(accountCreateAccountQueue, false);
    }
    @Bean
    Binding createAccountBinding() {
        return BindingBuilder
                .bind(accountCreateAccountQueue())
                .to(exchange())
                .with(accountCreateAccountRoutingkey)
                .noargs();
    }
    @Bean
    Queue accountFundingQueue() {
        return new Queue(accountFundingQueue, false);
    }
    @Bean
    Binding fundingBinding() {
        return BindingBuilder
                .bind(accountFundingQueue())
                .to(exchange())
                .with(accountFundingRoutingkey)
                .noargs();
    }
    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(host, port);
        cachingConnectionFactory.setUsername(username);
        cachingConnectionFactory.setPassword(password);
        cachingConnectionFactory.setVirtualHost(virtualHost);
        return cachingConnectionFactory;
    }
//    @Bean
//    public MessageConverter jsonMessageConverter() {
//        return new Jackson2JsonMessageConverter();
//    }
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
//        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}