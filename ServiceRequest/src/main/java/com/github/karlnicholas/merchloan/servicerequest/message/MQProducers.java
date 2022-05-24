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
    private final Destination accountCreateAccountQueue;
    private final Destination accountFundingQueue;
    private final Destination accountValidateCreditQueue;
    private final Destination accountValidateDebitQueue;
    private final Destination statementStatementQueue;
    private final Destination accountCloseLoanQueue;

    @Autowired
    public MQProducers(Connection connection, MQConsumerUtils mqConsumerUtils) throws JMSException {
        connection.setClientID("ServiceRequest");
        try ( Session session = connection.createSession() ) {
            accountCreateAccountQueue = session.createQueue(mqConsumerUtils.getAccountCreateAccountQueue());
            accountFundingQueue = session.createQueue(mqConsumerUtils.getAccountFundingQueue());
            accountValidateCreditQueue = session.createQueue(mqConsumerUtils.getAccountValidateCreditQueue());
            accountValidateDebitQueue = session.createQueue(mqConsumerUtils.getAccountValidateDebitQueue());
            statementStatementQueue = session.createQueue(mqConsumerUtils.getStatementStatementQueue());
            accountCloseLoanQueue = session.createQueue(mqConsumerUtils.getAccountCloseLoanQueue());
        }
        connection.start();
    }

    public void accountCreateAccount(Session session, CreateAccount createAccount) throws JMSException {
        log.debug("accountCreateAccount: {}", createAccount);
        try (MessageProducer producer = session.createProducer(accountCreateAccountQueue)) {
            producer.send(session.createObjectMessage(createAccount));
        }
    }

    public void accountFundLoan(Session session, FundLoan fundLoan) throws JMSException {
        log.debug("accountFundLoan: {}", fundLoan);
        try (MessageProducer producer = session.createProducer(accountFundingQueue)) {
            producer.send(session.createObjectMessage(fundLoan));
        }
    }

    public void accountValidateCredit(Session session, CreditLoan creditLoan) throws JMSException {
        log.debug("accountValidateCredit: {}", creditLoan);
        try (MessageProducer producer = session.createProducer(accountValidateCreditQueue)) {
            producer.send(session.createObjectMessage(creditLoan));
        }
    }

    public void accountValidateDebit(Session session, DebitLoan debitLoan) throws JMSException {
        log.debug("accountValidateDebit: {}", debitLoan);
        try (MessageProducer producer = session.createProducer(accountValidateDebitQueue)) {
            producer.send(session.createObjectMessage(debitLoan));
        }
    }

    public void statementStatement(Session session, StatementHeader statementHeader) throws JMSException {
        log.debug("statementStatement: {}", statementHeader);
        try (MessageProducer producer = session.createProducer(statementStatementQueue)) {
            producer.send(session.createObjectMessage(statementHeader));
        }
    }

    public void accountCloseLoan(Session session, CloseLoan closeLoan) throws JMSException {
        log.debug("accountCloseLoan: {}", closeLoan);
        try (MessageProducer producer = session.createProducer(accountCloseLoanQueue)) {
            producer.send(session.createObjectMessage(closeLoan));
        }
    }

}
