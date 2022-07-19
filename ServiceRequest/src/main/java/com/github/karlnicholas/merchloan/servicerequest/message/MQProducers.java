package com.github.karlnicholas.merchloan.servicerequest.message;

import com.github.karlnicholas.merchloan.jmsmessage.*;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.jms.*;

@ApplicationScoped
@Slf4j
public class MQProducers {
    @Resource(lookup = "java:comp/DefaultJMSConnectionFactory")
    private ConnectionFactory connectionFactory;
    private final JMSContext jmsContext;
    @Resource(lookup = "java:global/jms/queue/AccountCreateAccountQueue")
    private Queue accountCreateAccountQueue;
    private final JMSProducer accountCreateAccountProducer;
    @Resource(lookup = "java:global/jms/queue/AccountFundingQueue")
    private Queue accountFundingQueue;
    private final JMSProducer accountFundingProducer;
    @Resource(lookup = "java:global/jms/queue/AccountValidateCreditQueue")
    private Queue accountValidateCreditQueue;
    private final JMSProducer accountValidateCreditProducer;
    @Resource(lookup = "java:global/jms/queue/AccountValidateDebitQueue")
    private Queue accountValidateDebitQueue;
    private final JMSProducer accountValidateDebitProducer;
    @Resource(lookup = "java:global/jms/queue/StatementStatementQueue")
    private Queue statementStatementQueue;
    private final JMSProducer statementStatementProducer;
    @Resource(lookup = "java:global/jms/queue/AccountCloseLoanQueue")
    private Queue accountCloseLoanQueue;
    private final JMSProducer accountCloseLoanProducer;


    public MQProducers() {
        jmsContext = connectionFactory.createContext();
        accountCreateAccountProducer = jmsContext.createProducer().setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        accountFundingProducer = jmsContext.createProducer().setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        accountValidateCreditProducer = jmsContext.createProducer().setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        accountValidateDebitProducer = jmsContext.createProducer().setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        statementStatementProducer = jmsContext.createProducer().setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        accountCloseLoanProducer = jmsContext.createProducer().setDeliveryMode(DeliveryMode.NON_PERSISTENT);
    }
    @PreDestroy
    public void preDestroy() {
        jmsContext.close();
    }

    public void accountCreateAccount(CreateAccount createAccount) {
        log.debug("accountCreateAccount: {}", createAccount);
        accountCreateAccountProducer.send(accountCreateAccountQueue, jmsContext.createObjectMessage(createAccount));
    }

    public void accountFundLoan(FundLoan fundLoan) {
        log.debug("accountFundLoan: {}", fundLoan);
        accountFundingProducer.send(accountFundingQueue, jmsContext.createObjectMessage(fundLoan));
    }

    public void accountValidateCredit(CreditLoan creditLoan) {
        log.debug("accountValidateCredit: {}", creditLoan);
        accountValidateCreditProducer.send(accountValidateCreditQueue, jmsContext.createObjectMessage(creditLoan));
    }

    public void accountValidateDebit(DebitLoan debitLoan) {
        log.debug("accountValidateDebit: {}", debitLoan);
        accountValidateDebitProducer.send(accountValidateDebitQueue, jmsContext.createObjectMessage(debitLoan));
    }

    public void statementStatement(StatementHeader statementHeader) {
        log.debug("statementStatement: {}", statementHeader);
        statementStatementProducer.send(statementStatementQueue, jmsContext.createObjectMessage(statementHeader));
    }

    public void accountCloseLoan(CloseLoan closeLoan) {
        log.debug("accountCloseLoan: {}", closeLoan);
        accountCloseLoanProducer.send(accountCloseLoanQueue, jmsContext.createObjectMessage(closeLoan));
    }

}
