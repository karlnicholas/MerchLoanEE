package com.github.karlnicholas.merchloan.query.message;

import com.github.karlnicholas.merchloan.jms.MQConsumerUtils;
import com.github.karlnicholas.merchloan.jms.ReplyWaitingHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.jms.*;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@Slf4j
public class MQProducers {
    private final ReplyWaitingHandler replyWaitingHandler;
    private final Queue queryReplyQueue;
    private final JMSContext servicerequestQueryIdContext;
    private final Destination servicerequestQueryIdQueue;
//    private final JMSProducer servicerequestQueryIdProducer;
    private final JMSContext accountQueryAccountIdContext;
    private final Destination accountQueryAccountIdQueue;
//    private final JMSProducer accountQueryAccountIdProducer;
    private final JMSContext accountQueryLoanIdContext;
    private final Destination accountQueryLoanIdQueue;
//    private final JMSProducer accountQueryLoanIdProducer;
    private final JMSContext statementQueryStatementContext;
    private final Destination statementQueryStatementQueue;
//    private final JMSProducer statementQueryStatementProducer;
    private final JMSContext statementQueryStatementsContext;
    private final Destination statementQueryStatementsQueue;
//    private final JMSProducer statementQueryStatementsProducer;
    private final JMSContext serviceRequestCheckRequestContext;
    private final Destination serviceRequestCheckRequestQueue;
//    private final JMSProducer serviceRequestCheckRequestProducer;
    private final MQConsumerUtils mqConsumerUtils;

    public MQProducers(ConnectionFactory connectionFactory, MQConsumerUtils mqConsumerUtils) throws JMSException {
//        JMSContext jmsContext = connectionFactory.createContext();
//        connection.setClientID("Query");
        this.mqConsumerUtils = mqConsumerUtils;

        servicerequestQueryIdContext = connectionFactory.createContext();
        servicerequestQueryIdContext.setClientID("Query::servicerequestQueryIdContext");
        servicerequestQueryIdQueue = servicerequestQueryIdContext.createQueue(mqConsumerUtils.getServicerequestQueryIdQueue());
//        servicerequestQueryIdProducer = servicerequestQueryIdContext.createProducer();

        accountQueryAccountIdContext = connectionFactory.createContext();
        accountQueryAccountIdContext.setClientID("Query::accountQueryAccountIdContext");
        accountQueryAccountIdQueue = accountQueryAccountIdContext.createQueue(mqConsumerUtils.getAccountQueryAccountIdQueue());
//        accountQueryAccountIdProducer = accountQueryAccountIdContext.createProducer();

        accountQueryLoanIdContext = connectionFactory.createContext();
        accountQueryLoanIdContext.setClientID("Query::accountQueryLoanIdContext");
        accountQueryLoanIdQueue = accountQueryLoanIdContext.createQueue(mqConsumerUtils.getAccountQueryLoanIdQueue());
//        accountQueryLoanIdProducer = accountQueryLoanIdContext.createProducer();

        statementQueryStatementContext = connectionFactory.createContext();
        statementQueryStatementContext.setClientID("Query::statementQueryStatementContext");
        statementQueryStatementQueue = statementQueryStatementContext.createQueue(mqConsumerUtils.getStatementQueryStatementQueue());
//        statementQueryStatementProducer = statementQueryStatementContext.createProducer();

        statementQueryStatementsContext = connectionFactory.createContext();
        statementQueryStatementsContext.setClientID("Query::statementQueryStatementsContext");
        statementQueryStatementsQueue = statementQueryStatementsContext.createQueue(mqConsumerUtils.getStatementQueryStatementsQueue());
//        statementQueryStatementsProducer = statementQueryStatementsContext.createProducer();

        serviceRequestCheckRequestContext = connectionFactory.createContext();
        serviceRequestCheckRequestContext.setClientID("Query::serviceRequestCheckRequestContext");
        serviceRequestCheckRequestQueue = serviceRequestCheckRequestContext.createQueue(mqConsumerUtils.getServiceRequestCheckRequestQueue());
//        serviceRequestCheckRequestProducer = serviceRequestCheckRequestContext.createProducer();

        replyWaitingHandler = new ReplyWaitingHandler();
        JMSContext queueReplyContext = connectionFactory.createContext();
        queueReplyContext.setClientID("Query::queueReplyContext");
        queryReplyQueue = queueReplyContext.createTemporaryQueue();
        JMSConsumer replyConsumer = queueReplyContext.createConsumer(queryReplyQueue);
        replyConsumer.setMessageListener(replyWaitingHandler);
    }

    public Object queryServiceRequest(UUID id) {
        Instant start = Instant.now();
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        try {
            Message message = servicerequestQueryIdContext.createObjectMessage(id);
            message.setJMSCorrelationID(responseKey);
            message.setJMSReplyTo(queryReplyQueue);
            servicerequestQueryIdContext.createProducer().send(servicerequestQueryIdQueue, message);
            Object r = replyWaitingHandler.getReply(responseKey);
            log.debug("queryServiceRequest {}", Duration.between(Instant.now(), start));
            return r;
        } catch (InterruptedException | JMSException e) {
            log.error("queryServiceRequest", e);
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public Object queryAccount(UUID id) {
        Instant start = Instant.now();
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        try {
            Message message = accountQueryAccountIdContext.createObjectMessage(id);
            message.setJMSCorrelationID(responseKey);
            message.setJMSReplyTo(queryReplyQueue);
            accountQueryAccountIdContext.createProducer().send(accountQueryAccountIdQueue, message);
            Object r = replyWaitingHandler.getReply(responseKey);
            log.debug("queryAccount {}", Duration.between(Instant.now(), start));
            return r;
        } catch (JMSException | InterruptedException e) {
            log.error("queryAccount", e);
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public Object queryLoan(UUID id) {
        Instant start = Instant.now();
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        try {
            Message message = accountQueryLoanIdContext.createObjectMessage(id);
            message.setJMSCorrelationID(responseKey);
            message.setJMSReplyTo(queryReplyQueue);
            accountQueryLoanIdContext.createProducer().send(accountQueryLoanIdQueue, message);
            Object r = replyWaitingHandler.getReply(responseKey);
            log.debug("queryLoan {}", Duration.between(Instant.now(), start));
            return r;
        } catch (JMSException | InterruptedException e) {
            log.error("queryLoan", e);
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public Object queryStatement(UUID id) {
        Instant start = Instant.now();
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        try {
            Message message = statementQueryStatementContext.createObjectMessage(id);
            message.setJMSCorrelationID(responseKey);
            message.setJMSReplyTo(queryReplyQueue);
            statementQueryStatementContext.createProducer().send(statementQueryStatementQueue, message);
            Object r = replyWaitingHandler.getReply(responseKey);
            log.debug("queryStatement {}", Duration.between(Instant.now(), start));
            return r;
        } catch (JMSException | InterruptedException e) {
            log.error("queryStatement", e);
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public Object queryStatements(UUID id) {
        Instant start = Instant.now();
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        try {
            Message message = statementQueryStatementsContext.createObjectMessage(id);
            message.setJMSCorrelationID(responseKey);
            message.setJMSReplyTo(queryReplyQueue);
            statementQueryStatementsContext.createProducer().send(statementQueryStatementsQueue, message);
            Object r = replyWaitingHandler.getReply(responseKey);
            log.debug("queryStatements {}", Duration.between(Instant.now(), start));
            return r;
        } catch (JMSException | InterruptedException e) {
            log.error("queryStatements", e);
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public Object queryCheckRequest() {
        Instant start = Instant.now();
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        try {
            Message message = serviceRequestCheckRequestContext.createObjectMessage(new byte[0]);
            message.setJMSCorrelationID(responseKey);
            message.setJMSReplyTo(queryReplyQueue);
            serviceRequestCheckRequestContext.createProducer().send(serviceRequestCheckRequestQueue, message);
            Object r = replyWaitingHandler.getReply(responseKey);
            log.debug("queryCheckRequest {}", Duration.between(Instant.now(), start));
            return r;
        } catch (JMSException | InterruptedException e) {
            log.error("queryCheckRequest", e);
            Thread.currentThread().interrupt();
            return null;
        }
    }
}
