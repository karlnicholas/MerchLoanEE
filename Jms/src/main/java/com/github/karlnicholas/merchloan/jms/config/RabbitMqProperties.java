package com.github.karlnicholas.merchloan.jms.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "rabbitmq")
@PropertySource(value = "classpath:rabbitmq-config.properties")
@Data
public class RabbitMqProperties {
    private String host;
    private String port;
    private String virtualHost;
    private String password;
    private String username;
    private String exchange;
    private String accountCreateaccountRoutingKey;
    private String accountFundingRoutingKey;
    private String accountQueryAccountIdRoutingKey;
    private String accountQueryLoanIdRoutingKey;
    private String registerCreditLoanRoutingkey;
    private String registerDebitLoanRoutingkey;
    private String registerQueryLoanIdRoutingkey;
    private String servicerequestRoutingkey;
    private String servicerequestQueryIdRoutingkey;
}
