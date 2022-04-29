package com.github.karlnicholas.merchloan.query.message;

import com.github.karlnicholas.merchloan.jms.MQConsumerUtils;
import com.github.karlnicholas.merchloan.jms.ReplyWaitingHandler;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
public class MQProducers {
    private final MQConsumerUtils mqConsumerUtils;
    private final Channel querySendQueue;
    private final ReplyWaitingHandler replyWaitingHandler;
    private final String queryReplyQueue;

    public MQProducers(Connection connection, MQConsumerUtils mqConsumerUtils) throws IOException {
        this.mqConsumerUtils = mqConsumerUtils;
        replyWaitingHandler = new ReplyWaitingHandler();
        querySendQueue = connection.createChannel();
        queryReplyQueue = "query-reply-"+UUID.randomUUID();

        mqConsumerUtils.bindConsumer(connection, mqConsumerUtils.getExchange(), queryReplyQueue, true, replyWaitingHandler::handleReplies);
    }

    public Object queryServiceRequest(UUID id) {
        log.debug("queryServiceRequest: {}", id);
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(queryReplyQueue).build();
        try {
            querySendQueue.basicPublish(mqConsumerUtils.getExchange(), mqConsumerUtils.getServicerequestQueryIdQueue(), props, SerializationUtils.serialize(id));
            return replyWaitingHandler.getReply(responseKey);
        } catch (IOException | InterruptedException e) {
            log.error("queryServiceRequest", e);
            throw new RuntimeException(e);
        }
    }

    public Object queryAccount(UUID id) {
        log.debug("queryAccount: {}", id);
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(queryReplyQueue).build();
        try {
            querySendQueue.basicPublish(mqConsumerUtils.getExchange(), mqConsumerUtils.getAccountQueryAccountIdQueue(), props, SerializationUtils.serialize(id));
            return replyWaitingHandler.getReply(responseKey);
        } catch (IOException | InterruptedException e) {
            log.error("queryAccount", e);
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public Object queryLoan(UUID id) {
        log.debug("queryLoan: {}", id);
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(queryReplyQueue).build();
        try {
            querySendQueue.basicPublish(mqConsumerUtils.getExchange(), mqConsumerUtils.getAccountQueryLoanIdQueue(), props, SerializationUtils.serialize(id));
            return replyWaitingHandler.getReply(responseKey);
        } catch (IOException | InterruptedException e) {
            log.error("queryLoan", e);
            throw new RuntimeException(e);
        }
    }

    public Object queryStatement(UUID id) {
        log.debug("queryStatement: {}", id);
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(queryReplyQueue).build();
        try {
            querySendQueue.basicPublish(mqConsumerUtils.getExchange(), mqConsumerUtils.getStatementQueryStatementQueue(), props, SerializationUtils.serialize(id));
            return replyWaitingHandler.getReply(responseKey);
        } catch (IOException | InterruptedException e) {
            log.error("queryStatement", e);
            throw new RuntimeException(e);
        }
    }

    public Object queryStatements(UUID id) {
        log.debug("queryStatements: {}", id);
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(queryReplyQueue).build();
        try {
            querySendQueue.basicPublish(mqConsumerUtils.getExchange(), mqConsumerUtils.getStatementQueryStatementsQueue(), props, SerializationUtils.serialize(id));
            return replyWaitingHandler.getReply(responseKey);
        } catch (IOException | InterruptedException e) {
            log.error("queryStatements", e);
            throw new RuntimeException(e);
        }
    }

    public Object queryCheckRequest() {
        log.debug("queryCheckRequest:");
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(queryReplyQueue).build();
        try {
            querySendQueue.basicPublish(mqConsumerUtils.getExchange(), mqConsumerUtils.getServiceRequestCheckRequestQueue(), props, SerializationUtils.serialize(new byte[0]));
            return replyWaitingHandler.getReply(responseKey);
        } catch (IOException | InterruptedException e) {
            log.error("queryCheckRequest", e);
            throw new RuntimeException(e);
        }
    }
}
