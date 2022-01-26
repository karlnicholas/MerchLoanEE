package com.github.karlnicholas.merchloan.jms.message;

import com.github.karlnicholas.merchloan.apimessage.message.BillingCycleChargeRequest;
import com.github.karlnicholas.merchloan.jms.config.RabbitMqProperties;
import com.github.karlnicholas.merchloan.jmsmessage.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    public void accountCreateAccount(CreateAccount createAccount) {
        rabbitTemplate.convertAndSend(rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountCreateaccountRoutingKey(), createAccount);
    }

    public void accountFundLoan(FundLoan fundLoan) {
        rabbitTemplate.convertAndSend(rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountFundingRoutingKey(), fundLoan);
    }

    public void accountValidateCredit(CreditLoan creditLoan) {
        rabbitTemplate.convertAndSend(rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountValidateCreditRoutingkey(), creditLoan);
    }

    public void accountValidateDebit(DebitLoan debitLoan) {
        rabbitTemplate.convertAndSend(rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountValidateDebitRoutingkey(), debitLoan);
    }

    public Object queryAccount(UUID id) {
        return rabbitTemplate.convertSendAndReceive(rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountQueryAccountIdRoutingKey(), id);
    }

    public Object accountStatementHeader(StatementHeader statementHeader) {
        return rabbitTemplate.convertSendAndReceive(rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountStatementHeaderRoutingKey(), statementHeader);
    }

    public void registerFundLoan(DebitLoan debitLoan) {
        rabbitTemplate.convertAndSend(rabbitMqProperties.getExchange(), rabbitMqProperties.getRegisterFundLoanRoutingkey(), debitLoan);
    }

    public void registerCreditLoan(CreditLoan creditLoan) {
        rabbitTemplate.convertAndSend(rabbitMqProperties.getExchange(), rabbitMqProperties.getRegisterCreditLoanRoutingkey(), creditLoan);
    }

    public void registerDebitLoan(DebitLoan debitLoan) {
        rabbitTemplate.convertAndSend(rabbitMqProperties.getExchange(), rabbitMqProperties.getRegisterDebitLoanRoutingkey(), debitLoan);
    }

    public void statementStatement(StatementHeader statementHeader) {
        rabbitTemplate.convertAndSend(rabbitMqProperties.getExchange(), rabbitMqProperties.getStatementStatementRoutingkey(), statementHeader);
    }

    public Object registerStatementHeader(StatementHeader statementHeader) {
        return rabbitTemplate.convertSendAndReceive(rabbitMqProperties.getExchange(), rabbitMqProperties.getRegisterStatementHeaderRoutingkey(), statementHeader);
    }

    public void serviceRequestServiceRequest(ServiceRequestResponse serviceRequest) {
        rabbitTemplate.convertAndSend(rabbitMqProperties.getExchange(), rabbitMqProperties.getServicerequestRoutingkey(), serviceRequest);
    }

    public Object queryServiceRequest(UUID id) {
        return rabbitTemplate.convertSendAndReceive(rabbitMqProperties.getExchange(), rabbitMqProperties.getServicerequestQueryIdRoutingkey(), id);
    }

    public Object queryLoan(UUID id) {
        return rabbitTemplate.convertSendAndReceive(rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountQueryLoanIdRoutingKey(), id);
    }

    public Object queryRegister(UUID id) {
        return rabbitTemplate.convertSendAndReceive(rabbitMqProperties.getExchange(), rabbitMqProperties.getRegisterQueryLoanIdRoutingkey(), id);
    }

    public Object queryStatement(UUID id) {
        return rabbitTemplate.convertSendAndReceive(rabbitMqProperties.getExchange(), rabbitMqProperties.getStatementQueryStatementRoutingkey(), id);
    }

    public Object servicerequestCheckRequest(LocalDate businessDate) {
        return rabbitTemplate.convertSendAndReceive(rabbitMqProperties.getExchange(), rabbitMqProperties.getServiceRequestCheckRequestRoutingkey(), businessDate);
    }

    public Object acccountLoansToCycle(LocalDate businessDate) {
        return rabbitTemplate.convertSendAndReceive(rabbitMqProperties.getExchange(), rabbitMqProperties.getAcccountLoansToCycleRoutingkey(), businessDate);
    }

    public void serviceRequestBillLoan(BillingCycle billingCycle) {
        rabbitTemplate.convertAndSend(rabbitMqProperties.getExchange(), rabbitMqProperties.getServiceRequestBillLoanRoutingkey(), billingCycle);
    }

    public void serviceRequestBillingCycleCharge(BillingCycleChargeRequest billingCycleChargeRequest) {
        rabbitTemplate.convertAndSend(rabbitMqProperties.getExchange(), rabbitMqProperties.getServiceRequestBillingCycleChargeRoutingkey(), billingCycleChargeRequest);
    }

    public void registerBillingCycleCharge(BillingCycleCharge billingCycleCharge) {
        rabbitTemplate.convertAndSend(rabbitMqProperties.getExchange(), rabbitMqProperties.getRegisterBillingCycleChargeRoutingkey(), billingCycleCharge);
    }

    public void serviceRequestChargeCompleted(BillingCycleCharge billingCycleCharge) {
        rabbitTemplate.convertAndSend(rabbitMqProperties.getExchange(), rabbitMqProperties.getServiceRequestChargeCompletedRoutingkey(), billingCycleCharge);
    }
}