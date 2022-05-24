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
    private final Connection connection;
    private final ReplyWaitingHandler replyWaitingHandler;
    private final Queue queryReplyQueue;
    private final Destination servicerequestQueryIdQueue;
    private final Destination accountQueryAccountIdQueue;
    private final Destination accountQueryLoanIdQueue;
    private final Destination statementQueryStatementQueue;
    private final Destination statementQueryStatementsQueue;
//    private final Destination serviceRequestCheckRequestQueue;
    private final MQConsumerUtils mqConsumerUtils;

    public MQProducers(Connection connection, MQConsumerUtils mqConsumerUtils) throws JMSException {
        this.connection = connection;
        connection.setClientID("Query");
        try (Session session = connection.createSession() ) {
            this.mqConsumerUtils = mqConsumerUtils;
            replyWaitingHandler = new ReplyWaitingHandler();
            servicerequestQueryIdQueue = session.createQueue(mqConsumerUtils.getServicerequestQueryIdQueue());
            accountQueryAccountIdQueue = session.createQueue(mqConsumerUtils.getAccountQueryAccountIdQueue());
            accountQueryLoanIdQueue = session.createQueue(mqConsumerUtils.getAccountQueryLoanIdQueue());
            statementQueryStatementQueue = session.createQueue(mqConsumerUtils.getStatementQueryStatementQueue());
            statementQueryStatementsQueue = session.createQueue(mqConsumerUtils.getStatementQueryStatementsQueue());

        }
        Session consumerSession = connection.createSession();
        queryReplyQueue = consumerSession.createTemporaryQueue();
        mqConsumerUtils.bindConsumer(consumerSession, queryReplyQueue, replyWaitingHandler::onMessage);
        connection.start();
    }

    public Object queryServiceRequest(UUID id) {
        Instant start = Instant.now();
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        try (Session session = connection.createSession()) {
            Message message = session.createObjectMessage(id);
            message.setJMSCorrelationID(responseKey);
            message.setJMSReplyTo(queryReplyQueue);
            try (MessageProducer producer = session.createProducer(servicerequestQueryIdQueue)) {
                producer.send(message);
                Object r = replyWaitingHandler.getReply(responseKey);
                log.debug("queryServiceRequest {}", Duration.between(Instant.now(), start));
                return r;
            }
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
        try (Session session = connection.createSession()) {
            Message message = session.createObjectMessage(id);
            message.setJMSCorrelationID(responseKey);
            message.setJMSReplyTo(queryReplyQueue);
            try (MessageProducer producer = session.createProducer(accountQueryAccountIdQueue)) {
                producer.send(message);
                Object r = replyWaitingHandler.getReply(responseKey);
                log.debug("queryAccount {}", Duration.between(Instant.now(), start));
                return r;
            }
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
        try (Session session = connection.createSession()) {
            Message message = session.createObjectMessage(id);
            message.setJMSCorrelationID(responseKey);
            message.setJMSReplyTo(queryReplyQueue);
            try (MessageProducer producer = session.createProducer(accountQueryLoanIdQueue)) {
                producer.send(message);
                Object r = replyWaitingHandler.getReply(responseKey);
                log.debug("queryLoan {}", Duration.between(Instant.now(), start));
                return r;
            }
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
        try (Session session = connection.createSession()) {
            Message message = session.createObjectMessage(id);
            message.setJMSCorrelationID(responseKey);
            message.setJMSReplyTo(queryReplyQueue);
            try (MessageProducer producer = session.createProducer(statementQueryStatementQueue) ) {
                producer.send(message);
                Object r = replyWaitingHandler.getReply(responseKey);
                log.debug("queryStatement {}", Duration.between(Instant.now(), start));
                return r;
            }
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
        try (Session session = connection.createSession()) {
            Message message = session.createObjectMessage(id);
            message.setJMSCorrelationID(responseKey);
            message.setJMSReplyTo(queryReplyQueue);
            try (MessageProducer producer = session.createProducer(statementQueryStatementsQueue)) {
                producer.send(message);
                Object r = replyWaitingHandler.getReply(responseKey);
                log.debug("queryStatements {}", Duration.between(Instant.now(), start));
                return r;
            }
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
        try (Session session = connection.createSession()) {
            Queue serviceRequestCheckRequestQueue = session.createQueue(mqConsumerUtils.getServiceRequestCheckRequestQueue());
            Message message = session.createObjectMessage(new byte[0]);
            message.setJMSCorrelationID(responseKey);
            message.setJMSReplyTo(queryReplyQueue);
            try (MessageProducer producer = session.createProducer(serviceRequestCheckRequestQueue)) {
                producer.send(message);
                Object r = replyWaitingHandler.getReply(responseKey);
                log.debug("queryCheckRequest {}", Duration.between(Instant.now(), start));
                return r;
            }
        } catch (JMSException | InterruptedException e) {
            log.error("queryCheckRequest", e);
            Thread.currentThread().interrupt();
            return null;
        }
    }
}
