package com.github.karlnicholas.merchloan.statement.message;

import com.github.karlnicholas.merchloan.jms.MQConsumerUtils;
import com.github.karlnicholas.merchloan.jms.ReplyWaitingHandler;
import com.github.karlnicholas.merchloan.jmsmessage.BillingCycleCharge;
import com.github.karlnicholas.merchloan.jmsmessage.ServiceRequestResponse;
import com.github.karlnicholas.merchloan.jmsmessage.StatementCompleteResponse;
import com.github.karlnicholas.merchloan.jmsmessage.StatementHeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.jms.*;

import java.util.UUID;

@Component
@Slf4j
public class MQProducers {
    private final ReplyWaitingHandler replyWaitingHandler;
    private final Queue statementReplyQueue;
    private final JMSContext accountBillingCycleChargeContext;
    private final Destination accountBillingCycleChargeQueue;
    private final JMSContext accountQueryStatementHeaderContext;
    private final Destination accountQueryStatementHeaderQueue;
    private final JMSContext servicerequestContext;
    private final Destination servicerequestQueue;
    private final JMSContext accountLoanClosedContext;
    private final Destination accountLoanClosedQueue;
    private final JMSContext serviceRequestStatementCompleteContext;
    private final Destination serviceRequestStatementCompleteQueue;

    @Autowired
    public MQProducers(ConnectionFactory connectionFactory, MQConsumerUtils mqConsumerUtils) {

        accountBillingCycleChargeContext = connectionFactory.createContext();
        accountBillingCycleChargeContext.setClientID("Statement::accountBillingCycleChargeContext");
        accountBillingCycleChargeQueue = accountBillingCycleChargeContext.createQueue(mqConsumerUtils.getAccountBillingCycleChargeQueue());

        accountQueryStatementHeaderContext = connectionFactory.createContext();
        accountQueryStatementHeaderContext.setClientID("Statement::accountQueryStatementHeaderContext");
        accountQueryStatementHeaderQueue = accountQueryStatementHeaderContext.createQueue(mqConsumerUtils.getAccountQueryStatementHeaderQueue());

        servicerequestContext = connectionFactory.createContext();
        servicerequestContext.setClientID("Statement::servicerequestContext");
        servicerequestQueue = servicerequestContext.createQueue(mqConsumerUtils.getServicerequestQueue());

        accountLoanClosedContext = connectionFactory.createContext();
        accountLoanClosedContext.setClientID("Statement::accountLoanClosedContext");
        accountLoanClosedQueue = accountLoanClosedContext.createQueue(mqConsumerUtils.getAccountLoanClosedQueue());

        serviceRequestStatementCompleteContext = connectionFactory.createContext();
        serviceRequestStatementCompleteContext.setClientID("Statement::serviceRequestStatementCompleteContext");
        serviceRequestStatementCompleteQueue = serviceRequestStatementCompleteContext.createQueue(mqConsumerUtils.getServiceRequestStatementCompleteQueue());

        replyWaitingHandler = new ReplyWaitingHandler();
        JMSContext consumerContext = connectionFactory.createContext();
        consumerContext.setClientID("Statement::replyWaitingHandler");
        statementReplyQueue = consumerContext.createTemporaryQueue();
        consumerContext.createConsumer(statementReplyQueue).setMessageListener(replyWaitingHandler::onMessage);
    }

    public Object accountBillingCycleCharge(BillingCycleCharge billingCycleCharge) throws InterruptedException, JMSException {
        log.debug("accountBillingCycleCharge: {}", billingCycleCharge);
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        Message message = accountBillingCycleChargeContext.createObjectMessage(billingCycleCharge);
        message.setJMSCorrelationID(responseKey);
        message.setJMSReplyTo(statementReplyQueue);
        accountBillingCycleChargeContext.createProducer().send(accountBillingCycleChargeQueue, message);
        return replyWaitingHandler.getReply(responseKey);
    }

    public Object accountQueryStatementHeader(StatementHeader statementHeader) throws InterruptedException, JMSException {
        log.debug("accountQueryStatementHeader: {}", statementHeader);
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        Message message = accountQueryStatementHeaderContext.createObjectMessage(statementHeader);
        message.setJMSCorrelationID(responseKey);
        message.setJMSReplyTo(statementReplyQueue);
        accountQueryStatementHeaderContext.createProducer().send(accountQueryStatementHeaderQueue, message);
        return replyWaitingHandler.getReply(responseKey);
    }

    public void serviceRequestServiceRequest(ServiceRequestResponse serviceRequest) {
        log.debug("serviceRequestServiceRequest: {}", serviceRequest);
        servicerequestContext.createProducer().send(servicerequestQueue, servicerequestContext.createObjectMessage(serviceRequest));
    }

    public void accountLoanClosed(StatementHeader statementHeader) throws JMSException {
        log.debug("accountLoanClosed: {}", statementHeader);
        accountLoanClosedContext.createProducer().send(accountLoanClosedQueue, accountLoanClosedContext.createObjectMessage(statementHeader));
    }

    public void serviceRequestStatementComplete(StatementCompleteResponse requestResponse) throws JMSException {
        log.debug("serviceRequestStatementComplete: {}", requestResponse);
        serviceRequestStatementCompleteContext.createProducer().send(serviceRequestStatementCompleteQueue, serviceRequestStatementCompleteContext.createObjectMessage(requestResponse));
    }

}
