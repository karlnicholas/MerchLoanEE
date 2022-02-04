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
    private String accountValidateCreditRoutingkey;
    private String accountValidateDebitRoutingkey;
    private String accountQueryAccountIdRoutingKey;
    private String accountQueryLoanIdRoutingKey;
    private String accountStatementHeaderRoutingKey;
    private String accountValidateCloseRoutingkey;
    private String acccountLoansToCycleRoutingkey;

    private String registerFundLoanRoutingkey;
    private String registerCreditLoanRoutingkey;
    private String registerDebitLoanRoutingkey;
    private String registerStatementHeaderRoutingkey;
    private String registerCloseLoanRoutingkey;
    private String registerQueryLoanIdRoutingkey;
    private String registerBillingCycleChargeRoutingkey;

    private String servicerequestRoutingkey;
    private String servicerequestQueryIdRoutingkey;
    private String serviceRequestCheckRequestRoutingkey;
    private String serviceRequestBillLoanRoutingkey;
    private String serviceRequestBillingCycleChargeRoutingkey;
    private String serviceRequestChargeCompletedRoutingkey;

    private String statementStatementRoutingkey;
    private String statementQueryStatementRoutingkey;
    private String statementQueryStatementsRoutingkey;
    private String statementQueryMostRecentStatementRoutingkey;
}
