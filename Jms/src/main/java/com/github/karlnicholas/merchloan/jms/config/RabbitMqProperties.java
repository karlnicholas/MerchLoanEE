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
    private String accountCreateaccountQueue;
    private String accountCreateaccountRoutingKey;
    private String accountFundingQueue;
    private String accountFundingRoutingKey;
    private String accountQueryAccountIdQueue;
    private String accountQueryAccountIdRoutingKey;
    private String accountQueryLoanIdQueue;
    private String accountQueryLoanIdRoutingKey;
    private String accountQueryLoanLenderIdQueue;
    private String accountQueryLoanLenderIdRoutingKey;
    private String accountQueryLoanLenderNameQueue;
    private String accountQueryLoanLenderNameRoutingKey;
    private String ledgerCredittoloanQueue;
    private String ledgerCredittoloanRoutingkey;
    private String ledgerDebitfromloanQueue;
    private String ledgerDebitfromloanRoutingkey;
    private String ledgerQueryLoanIdQueue;
    private String ledgerQueryLoanIdRoutingkey;
    private String servicerequestQueue;
    private String servicerequestRoutingkey;
    private String servicerequestQueryIdQueue;
    private String servicerequestQueryIdRoutingkey;
}
