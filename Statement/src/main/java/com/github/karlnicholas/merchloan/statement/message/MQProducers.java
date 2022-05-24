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
    private final Destination accountBillingCycleChargeQueue;
    private final Destination accountQueryStatementHeaderQueue;
    private final Destination servicerequestQueue;
    private final Destination accountLoanClosedQueue;
    private final Destination serviceRequestStatementCompleteQueue;

    @Autowired
    public MQProducers(Connection connection, MQConsumerUtils mqConsumerUtils) throws JMSException {
        try (Session session = connection.createSession()) {
            replyWaitingHandler = new ReplyWaitingHandler();
            accountBillingCycleChargeQueue = session.createQueue(mqConsumerUtils.getAccountBillingCycleChargeQueue());
            accountQueryStatementHeaderQueue = session.createQueue(mqConsumerUtils.getAccountQueryStatementHeaderQueue());
            servicerequestQueue = session.createQueue(mqConsumerUtils.getServicerequestQueue());
            accountLoanClosedQueue = session.createQueue(mqConsumerUtils.getAccountLoanClosedQueue());
            serviceRequestStatementCompleteQueue = session.createQueue(mqConsumerUtils.getServiceRequestStatementCompleteQueue());
        }
        Session consumerSession = connection.createSession();
        statementReplyQueue = consumerSession.createTemporaryQueue();
        mqConsumerUtils.bindConsumer(consumerSession, statementReplyQueue, replyWaitingHandler::onMessage);
        connection.start();
    }

    public Object accountBillingCycleCharge(Session session, BillingCycleCharge billingCycleCharge) throws InterruptedException, JMSException {
        log.debug("accountBillingCycleCharge: {}", billingCycleCharge);
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        Message message = session.createObjectMessage(billingCycleCharge);
        message.setJMSCorrelationID(responseKey);
        message.setJMSReplyTo(statementReplyQueue);
        try (MessageProducer producer = session.createProducer(accountBillingCycleChargeQueue)) {
            producer.send(message);
            return replyWaitingHandler.getReply(responseKey);
        }
    }

    public Object accountQueryStatementHeader(Session session, StatementHeader statementHeader) throws InterruptedException, JMSException {
        log.debug("accountQueryStatementHeader: {}", statementHeader);
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        Message message = session.createObjectMessage(statementHeader);
        message.setJMSCorrelationID(responseKey);
        message.setJMSReplyTo(statementReplyQueue);
        try (MessageProducer producer = session.createProducer(accountQueryStatementHeaderQueue)) {
            producer.send(message);
            return replyWaitingHandler.getReply(responseKey);
        }
    }

    public void serviceRequestServiceRequest(Session session, ServiceRequestResponse serviceRequest) throws JMSException {
        log.debug("serviceRequestServiceRequest: {}", serviceRequest);
        try (MessageProducer producer = session.createProducer(servicerequestQueue)) {
            producer.send(session.createObjectMessage(serviceRequest));
        }
    }

    public void accountLoanClosed(Session session, StatementHeader statementHeader) throws JMSException {
        log.debug("accountLoanClosed: {}", statementHeader);
        try (MessageProducer producer = session.createProducer(accountLoanClosedQueue)) {
            producer.send(session.createObjectMessage(statementHeader));
        }
    }

    public void serviceRequestStatementComplete(Session session, StatementCompleteResponse requestResponse) throws JMSException {
        log.debug("serviceRequestStatementComplete: {}", requestResponse);
        try (MessageProducer producer = session.createProducer(serviceRequestStatementCompleteQueue)) {
            producer.send(session.createObjectMessage(requestResponse));
        }
    }

}
