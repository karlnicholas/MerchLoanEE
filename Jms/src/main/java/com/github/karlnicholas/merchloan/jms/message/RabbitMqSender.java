package com.github.karlnicholas.merchloan.jms.message;

import com.github.karlnicholas.merchloan.jms.config.RabbitMqProperties;
import com.github.karlnicholas.merchloan.jmsmessage.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RabbitMqSender {
    private final RabbitTemplate rabbitTemplate;
    private final RabbitMqProperties rabbitMqProperties;

    @Autowired
    public RabbitMqSender(RabbitTemplate rabbitTemplate, RabbitMqProperties rabbitMqProperties) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitMqProperties = rabbitMqProperties;
    }

    public void sendCreateAccount(CreateAccount createAccount) {
        rabbitTemplate.convertAndSend(rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountCreateaccountRoutingKey(), createAccount);
    }

    public void sendFundingRequest(FundLoan fundLoan) {
        rabbitTemplate.convertAndSend(rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountFundingRoutingKey(), fundLoan);
    }

    public void sendCreditLoan(CreditLoan creditLoan) {
        rabbitTemplate.convertAndSend(rabbitMqProperties.getExchange(), rabbitMqProperties.getRegisterCreditLoanRoutingkey(), creditLoan);
    }

    public void sendDebitLoan(DebitLoan debitLoan) {
        rabbitTemplate.convertAndSend(rabbitMqProperties.getExchange(), rabbitMqProperties.getRegisterDebitLoanRoutingkey(), debitLoan);
    }

    public void sendServiceRequest(ServiceRequestResponse serviceRequest) {
        rabbitTemplate.convertAndSend(rabbitMqProperties.getExchange(), rabbitMqProperties.getServicerequestRoutingkey(), serviceRequest);
    }

    public Object queryServiceRequest(UUID id) {
        return rabbitTemplate.convertSendAndReceive(rabbitMqProperties.getExchange(), rabbitMqProperties.getServicerequestQueryIdRoutingkey(), id);
    }

    public Object queryAccount(UUID id) {
        return rabbitTemplate.convertSendAndReceive(rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountQueryAccountIdRoutingKey(), id);
    }

    public Object queryLoan(UUID id) {
        return rabbitTemplate.convertSendAndReceive(rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountQueryLoanIdRoutingKey(), id);
    }

    public Object queryRegister(UUID id) {
        return rabbitTemplate.convertSendAndReceive(rabbitMqProperties.getExchange(), rabbitMqProperties.getRegisterQueryLoanIdRoutingkey(), id);
    }
}