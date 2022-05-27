package com.github.karlnicholas.merchloan.servicerequest.message;

import com.github.karlnicholas.merchloan.jmsmessage.*;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.Queue;

@ApplicationScoped
@Slf4j
public class MQProducers {
    @Resource(lookup = "java:jboss/exported/jms/RemoteConnectionFactory")
    private ConnectionFactory connectionFactory;
    @Resource(lookup = "java:global/jms/queue/AccountCreateAccountQueue")
    private Queue accountCreateAccountQueue;
    @Resource(lookup = "java:global/jms/queue/AccountFundingQueue")
    private Queue accountFundingQueue;
    @Resource(lookup = "java:global/jms/queue/AccountValidateCreditQueue")
    private Queue accountValidateCreditQueue;
    @Resource(lookup = "java:global/jms/queue/AccountValidateDebitQueue")
    private Queue accountValidateDebitQueue;
    @Resource(lookup = "java:global/jms/queue/StatementStatementQueue")
    private Queue statementStatementQueue;
    @Resource(lookup = "java:global/jms/queue/AccountCloseLoanQueue")
    private Queue accountCloseLoanQueue;

    public void accountCreateAccount(CreateAccount createAccount) {
        log.debug("accountCreateAccount: {}", createAccount);
        try (JMSContext jmsContext = connectionFactory.createContext()) {
            jmsContext.createProducer().send(accountCreateAccountQueue, jmsContext.createObjectMessage(createAccount));
        }
    }

    public void accountFundLoan(FundLoan fundLoan) {
        log.debug("accountFundLoan: {}", fundLoan);
        try (JMSContext jmsContext = connectionFactory.createContext()) {
            jmsContext.createProducer().send(accountFundingQueue, jmsContext.createObjectMessage(fundLoan));
        }
    }

    public void accountValidateCredit(CreditLoan creditLoan) {
        log.debug("accountValidateCredit: {}", creditLoan);
        try (JMSContext jmsContext = connectionFactory.createContext()) {
            jmsContext.createProducer().send(accountValidateCreditQueue, jmsContext.createObjectMessage(creditLoan));

        }
    }

    public void accountValidateDebit(DebitLoan debitLoan) {
        log.debug("accountValidateDebit: {}", debitLoan);
        try (JMSContext jmsContext = connectionFactory.createContext()) {
            jmsContext.createProducer().send(accountValidateDebitQueue, jmsContext.createObjectMessage(debitLoan));
        }
    }

    public void statementStatement(StatementHeader statementHeader) {
        log.debug("statementStatement: {}", statementHeader);
        try (JMSContext jmsContext = connectionFactory.createContext()) {
            jmsContext.createProducer().send(statementStatementQueue, jmsContext.createObjectMessage(statementHeader));
        }
    }

    public void accountCloseLoan(CloseLoan closeLoan) {
        log.debug("accountCloseLoan: {}", closeLoan);
        try (JMSContext jmsContext = connectionFactory.createContext()) {
            jmsContext.createProducer().send(accountCloseLoanQueue, jmsContext.createObjectMessage(closeLoan));
        }
    }

}
