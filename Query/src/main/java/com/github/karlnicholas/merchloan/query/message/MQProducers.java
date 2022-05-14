package com.github.karlnicholas.merchloan.query.message;

import com.github.karlnicholas.merchloan.jms.MQConsumerUtils;
import com.github.karlnicholas.merchloan.jms.ReplyWaitingHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.jms.*;
import java.util.UUID;

@Service
@Slf4j
public class MQProducers {
    private final Session session;
    private final MessageProducer queryProducer;
    private final ReplyWaitingHandler replyWaitingHandler;
    private final Destination queryReplyQueue;
    private final Destination servicerequestQueryIdQueue;
    private final Destination accountQueryAccountIdQueue;
    private final Destination accountQueryLoanIdQueue;
    private final Destination statementQueryStatementQueue;
    private final Destination statementQueryStatementsQueue;
    private final Destination serviceRequestCheckRequestQueue;

    public MQProducers(Session session, MQConsumerUtils mqConsumerUtils) throws JMSException {
        this.session = session;
        this.queryProducer = session.createProducer(null);
        replyWaitingHandler = new ReplyWaitingHandler();
        queryReplyQueue = session.createTemporaryQueue();
        servicerequestQueryIdQueue = session.createQueue(mqConsumerUtils.getServicerequestQueryIdQueue());
        accountQueryAccountIdQueue = session.createQueue(mqConsumerUtils.getAccountQueryAccountIdQueue());
        accountQueryLoanIdQueue = session.createQueue(mqConsumerUtils.getAccountQueryLoanIdQueue());
        statementQueryStatementQueue = session.createQueue(mqConsumerUtils.getStatementQueryStatementQueue());
        statementQueryStatementsQueue = session.createQueue(mqConsumerUtils.getStatementQueryStatementsQueue());
        serviceRequestCheckRequestQueue = session.createQueue(mqConsumerUtils.getServiceRequestCheckRequestQueue());
        mqConsumerUtils.bindConsumer(session, queryReplyQueue, replyWaitingHandler::onMessage);
    }

    public Object queryServiceRequest(UUID id) {
        log.debug("queryServiceRequest: {}", id);
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        try {
            Message message = session.createObjectMessage(id);
            message.setJMSCorrelationID(responseKey);
            message.setJMSReplyTo(queryReplyQueue);
            queryProducer.send(servicerequestQueryIdQueue, message);
            return replyWaitingHandler.getReply(responseKey);
        } catch (InterruptedException | JMSException e) {
            log.error("queryServiceRequest", e);
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public Object queryAccount(UUID id) {
        log.debug("queryAccount: {}", id);
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        try {
            Message message = session.createObjectMessage(id);
            message.setJMSCorrelationID(responseKey);
            message.setJMSReplyTo(queryReplyQueue);
            queryProducer.send(accountQueryAccountIdQueue, message);
            return replyWaitingHandler.getReply(responseKey);
        } catch (JMSException | InterruptedException e) {
            log.error("queryAccount", e);
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public Object queryLoan(UUID id) {
        log.debug("queryLoan: {}", id);
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        try {
            Message message = session.createObjectMessage(id);
            message.setJMSCorrelationID(responseKey);
            message.setJMSReplyTo(queryReplyQueue);
            queryProducer.send(accountQueryLoanIdQueue, message);
            return replyWaitingHandler.getReply(responseKey);
        } catch (JMSException | InterruptedException e) {
            log.error("queryLoan", e);
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public Object queryStatement(UUID id) {
        log.debug("queryStatement: {}", id);
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        try {
            Message message = session.createObjectMessage(id);
            message.setJMSCorrelationID(responseKey);
            message.setJMSReplyTo(queryReplyQueue);
            queryProducer.send(statementQueryStatementQueue, message);
            return replyWaitingHandler.getReply(responseKey);
        } catch (JMSException | InterruptedException e) {
            log.error("queryStatement", e);
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public Object queryStatements(UUID id) {
        log.debug("queryStatements: {}", id);
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        try {
            Message message = session.createObjectMessage(id);
            message.setJMSCorrelationID(responseKey);
            message.setJMSReplyTo(queryReplyQueue);
            queryProducer.send(statementQueryStatementsQueue, message);
            return replyWaitingHandler.getReply(responseKey);
        } catch (JMSException | InterruptedException e) {
            log.error("queryStatements", e);
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public Object queryCheckRequest() {
        log.debug("queryCheckRequest:");
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        try {
            Message message = session.createObjectMessage(new byte[0]);
            message.setJMSCorrelationID(responseKey);
            message.setJMSReplyTo(queryReplyQueue);
            queryProducer.send(serviceRequestCheckRequestQueue, message);
            return replyWaitingHandler.getReply(responseKey);
        } catch (JMSException | InterruptedException e) {
            log.error("queryCheckRequest", e);
            Thread.currentThread().interrupt();
            return null;
        }
    }
}
