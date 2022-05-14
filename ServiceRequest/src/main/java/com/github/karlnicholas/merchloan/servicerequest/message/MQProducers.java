package com.github.karlnicholas.merchloan.servicerequest.message;

import com.github.karlnicholas.merchloan.jms.MQConsumerUtils;
import com.github.karlnicholas.merchloan.jmsmessage.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

@Service
@Slf4j
public class MQProducers {
    private final Session session;
    private final MessageProducer serviceRequestProducer;
    private final Destination accountCreateAccountQueue;
    private final Destination accountFundingQueue;
    private final Destination accountValidateCreditQueue;
    private final Destination accountValidateDebitQueue;
    private final Destination statementStatementQueue;
    private final Destination accountCloseLoanQueue;

    @Autowired
    public MQProducers(Session session, MQConsumerUtils mqConsumerUtils) throws JMSException {
        this.session = session;
        serviceRequestProducer = session.createProducer(null);
        accountCreateAccountQueue = session.createQueue(mqConsumerUtils.getAccountCreateAccountQueue());
        accountFundingQueue = session.createQueue(mqConsumerUtils.getAccountFundingQueue());
        accountValidateCreditQueue = session.createQueue(mqConsumerUtils.getAccountValidateCreditQueue());
        accountValidateDebitQueue = session.createQueue(mqConsumerUtils.getAccountValidateDebitQueue());
        statementStatementQueue = session.createQueue(mqConsumerUtils.getStatementStatementQueue());
        accountCloseLoanQueue = session.createQueue(mqConsumerUtils.getAccountCloseLoanQueue());
    }

    public void accountCreateAccount(CreateAccount createAccount) throws JMSException {
        log.debug("accountCreateAccount: {}", createAccount);
        serviceRequestProducer.send(accountCreateAccountQueue, session.createObjectMessage(createAccount));
    }

    public void accountFundLoan(FundLoan fundLoan) throws JMSException {
        log.debug("accountFundLoan: {}", fundLoan);
        serviceRequestProducer.send(accountFundingQueue, session.createObjectMessage(fundLoan));
    }

    public void accountValidateCredit(CreditLoan creditLoan) throws JMSException {
        log.debug("accountValidateCredit: {}", creditLoan);
        serviceRequestProducer.send(accountValidateCreditQueue, session.createObjectMessage(creditLoan));
    }

    public void accountValidateDebit(DebitLoan debitLoan) throws JMSException {
        log.debug("accountValidateDebit: {}", debitLoan);
        serviceRequestProducer.send(accountValidateDebitQueue, session.createObjectMessage(debitLoan));
    }

    public void statementStatement(StatementHeader statementHeader) throws JMSException {
        log.debug("statementStatement: {}", statementHeader);
        serviceRequestProducer.send(statementStatementQueue, session.createObjectMessage(statementHeader));
    }

    public void accountCloseLoan(CloseLoan closeLoan) throws JMSException {
        log.debug("accountCloseLoan: {}", closeLoan);
        serviceRequestProducer.send(accountCloseLoanQueue, session.createObjectMessage(closeLoan));
    }

}
