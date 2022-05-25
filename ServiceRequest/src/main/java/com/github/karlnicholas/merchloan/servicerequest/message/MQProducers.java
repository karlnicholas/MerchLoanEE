package com.github.karlnicholas.merchloan.servicerequest.message;

import com.github.karlnicholas.merchloan.jms.MQConsumerUtils;
import com.github.karlnicholas.merchloan.jmsmessage.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.jms.*;

@Service
@Slf4j
public class MQProducers {
    private final JMSContext accountCreateAccountContext;
    private final Destination accountCreateAccountQueue;
//    private final JMSProducer accountCreateAccountProducer;
    private final JMSContext accountFundingContext;
    private final Destination accountFundingQueue;
//    private final JMSProducer accountFundingProducer;
    private final JMSContext accountValidateCreditContext;
    private final Destination accountValidateCreditQueue;
//    private final JMSProducer accountValidateCreditProducer;
    private final JMSContext accountValidateDebitContext;
    private final Destination accountValidateDebitQueue;
//    private final JMSProducer accountValidateDebitProducer;
    private final JMSContext statementStatementContext;
    private final Destination statementStatementQueue;
//    private final JMSProducer statementStatementProducer;
    private final JMSContext accountCloseLoanContext;
    private final Destination accountCloseLoanQueue;
//    private final JMSProducer accountCloseLoanProducer;

    @Autowired
    public MQProducers(ConnectionFactory connectionFactory, MQConsumerUtils mqConsumerUtils) throws JMSException {
        accountCreateAccountContext = connectionFactory.createContext();
        accountCreateAccountContext.setClientID("ServiceRequest::accountCreateAccountContext");
        accountCreateAccountQueue = accountCreateAccountContext.createQueue(mqConsumerUtils.getAccountCreateAccountQueue());

        accountFundingContext = connectionFactory.createContext();
        accountFundingContext.setClientID("ServiceRequest::accountFundingContext");
        accountFundingQueue = accountFundingContext.createQueue(mqConsumerUtils.getAccountFundingQueue());

        accountValidateCreditContext = connectionFactory.createContext();
        accountValidateCreditContext.setClientID("ServiceRequest::accountValidateCreditContext");
        accountValidateCreditQueue = accountValidateCreditContext.createQueue(mqConsumerUtils.getAccountValidateCreditQueue());

        accountValidateDebitContext = connectionFactory.createContext();
        accountValidateDebitContext.setClientID("ServiceRequest::accountValidateDebitContext");
        accountValidateDebitQueue = accountValidateDebitContext.createQueue(mqConsumerUtils.getAccountValidateDebitQueue());

        statementStatementContext = connectionFactory.createContext();
        statementStatementContext.setClientID("ServiceRequest::statementStatementContext");
        statementStatementQueue = statementStatementContext.createQueue(mqConsumerUtils.getStatementStatementQueue());

        accountCloseLoanContext = connectionFactory.createContext();
        accountCloseLoanContext.setClientID("ServiceRequest::accountCloseLoanContext");
        accountCloseLoanQueue = accountCloseLoanContext.createQueue(mqConsumerUtils.getAccountCloseLoanQueue());
    }

    public void accountCreateAccount(CreateAccount createAccount) throws JMSException {
        log.debug("accountCreateAccount: {}", createAccount);
        accountCreateAccountContext.createProducer().send(accountCreateAccountQueue, accountCreateAccountContext.createObjectMessage(createAccount));
    }

    public void accountFundLoan(FundLoan fundLoan) throws JMSException {
        log.debug("accountFundLoan: {}", fundLoan);
        accountFundingContext.createProducer().send(accountFundingQueue, accountFundingContext.createObjectMessage(fundLoan));
    }

    public void accountValidateCredit(CreditLoan creditLoan) throws JMSException {
        log.debug("accountValidateCredit: {}", creditLoan);
        accountValidateCreditContext.createProducer().send(accountValidateCreditQueue, accountValidateCreditContext.createObjectMessage(creditLoan));
    }

    public void accountValidateDebit(DebitLoan debitLoan) throws JMSException {
        log.debug("accountValidateDebit: {}", debitLoan);
        accountValidateDebitContext.createProducer().send(accountValidateDebitQueue, accountValidateDebitContext.createObjectMessage(debitLoan));
    }

    public void statementStatement(StatementHeader statementHeader) throws JMSException {
        log.debug("statementStatement: {}", statementHeader);
        statementStatementContext.createProducer().send(statementStatementQueue, statementStatementContext.createObjectMessage(statementHeader));
    }

    public void accountCloseLoan(CloseLoan closeLoan) throws JMSException {
        log.debug("accountCloseLoan: {}", closeLoan);
        accountCloseLoanContext.createProducer().send(accountCloseLoanQueue, accountCloseLoanContext.createObjectMessage(closeLoan));
    }

}
