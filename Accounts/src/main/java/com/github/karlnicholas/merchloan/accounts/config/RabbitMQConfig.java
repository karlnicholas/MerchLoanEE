package com.github.karlnicholas.merchloan.accounts.config;

import com.github.karlnicholas.merchloan.jms.config.RabbitMqProperties;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    private final RabbitMqProperties rabbitMqProperties;
    @Value("${rabbitmq.account.createaccount.queue}")
    String accountCreateaccountQueue;
    @Value("${rabbitmq.account.funding.queue}")
    String accountFundingaccountQueue;
    @Value("${rabbitmq.account.validate.credit.queue}")
    String accountValidateCreditQueue;
    @Value("${rabbitmq.account.validate.debit.queue}")
    String accountValidateDebitQueue;
    @Value("${rabbitmq.account.statementheader.queue}")
    String accountStatementHeaderQueue;
    @Value("${rabbitmq.account.query.account.id.queue}")
    String accountQueryAccountIdQueue;
    @Value("${rabbitmq.account.query.loan.id.queue}")
    String accountQueryLoanIdQueue;
    @Value("${rabbitmq.account.loanstocycle.queue}")
    String accountLoansToCycleQueue;
    @Value("${rabbitmq.account.closeloan.queue}")
    String accountCloseLoanQueue;

    public RabbitMQConfig(RabbitMqProperties rabbitMqProperties) {
        this.rabbitMqProperties = rabbitMqProperties;
    }

    @Bean
    public Exchange exchange() {
        return ExchangeBuilder.directExchange(rabbitMqProperties.getExchange()).durable(false).build();
    }

    @Bean
    public Queue accountCreateAccountQueue() {
        return new Queue(accountCreateaccountQueue, false);
    }
    @Bean
    public Binding createAccountBinding() {
        return BindingBuilder
                .bind(accountCreateAccountQueue())
                .to(exchange())
                .with(rabbitMqProperties.getAccountCreateaccountRoutingKey())
                .noargs();
    }
    @Bean
    public Queue accountFundingQueue() {
        return new Queue(accountFundingaccountQueue, false);
    }
    @Bean
    public Binding fundingBinding() {
        return BindingBuilder
                .bind(accountFundingQueue())
                .to(exchange())
                .with(rabbitMqProperties.getAccountFundingRoutingKey())
                .noargs();
    }

    @Bean
    public Queue accountValidateCreditQueue() {
        return new Queue(accountValidateCreditQueue, false);
    }
    @Bean
    public Binding validateCreditBinding() {
        return BindingBuilder
                .bind(accountValidateCreditQueue())
                .to(exchange())
                .with(rabbitMqProperties.getAccountValidateCreditRoutingkey())
                .noargs();
    }

    @Bean
    public Queue accountValidateDebitQueue() {
        return new Queue(accountValidateDebitQueue, false);
    }
    @Bean
    public Binding validateDebit() {
        return BindingBuilder
                .bind(accountValidateDebitQueue())
                .to(exchange())
                .with(rabbitMqProperties.getAccountValidateDebitRoutingkey())
                .noargs();
    }

    @Bean
    public Queue statementHeaderQueue() {
        return new Queue(accountStatementHeaderQueue, false);
    }
    @Bean
    public Binding statementHeaderBinding() {
        return BindingBuilder
                .bind(statementHeaderQueue())
                .to(exchange())
                .with(rabbitMqProperties.getAccountStatementHeaderRoutingKey())
                .noargs();
    }

    @Bean
    public Queue accountQueryAccountIdQueue() {
        return new Queue(accountQueryAccountIdQueue, false);
    }
    @Bean
    public Binding createAccountQueryAccountIdBinding() {
        return BindingBuilder
                .bind(accountQueryAccountIdQueue())
                .to(exchange())
                .with(rabbitMqProperties.getAccountQueryAccountIdRoutingKey())
                .noargs();
    }

    @Bean
    public Queue accountQueryLoanIdQueue() {
        return new Queue(accountQueryLoanIdQueue, false);
    }
    @Bean
    public Binding createAccountQueryLoanIdBinding() {
        return BindingBuilder
                .bind(accountQueryLoanIdQueue())
                .to(exchange())
                .with(rabbitMqProperties.getAccountQueryLoanIdRoutingKey())
                .noargs();
    }

    @Bean
    public Queue accountLoansToCycleQueue() {
        return new Queue(accountLoansToCycleQueue, false);
    }
    @Bean
    public Binding accountLoansToCycleBinding() {
        return BindingBuilder
                .bind(accountLoansToCycleQueue())
                .to(exchange())
                .with(rabbitMqProperties.getAcccountLoansToCycleRoutingkey())
                .noargs();
    }

    @Bean
    public Queue accountCloseLoanQueue() {
        return new Queue(accountCloseLoanQueue, false);
    }
    @Bean
    public Binding validateClose() {
        return BindingBuilder
                .bind(accountCloseLoanQueue())
                .to(exchange())
                .with(rabbitMqProperties.getAccountCloseLoanRoutingkey())
                .noargs();
    }
}