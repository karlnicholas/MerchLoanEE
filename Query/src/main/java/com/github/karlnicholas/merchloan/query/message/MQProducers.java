package com.github.karlnicholas.merchloan.query.message;

import com.github.karlnicholas.merchloan.jms.ReplyWaitingHandler;
import com.github.karlnicholas.merchloan.jms.config.MQQueueNames;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
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
    private final MQQueueNames MQQueueNames;
    private final Channel querySendQueue;
    private final ReplyWaitingHandler replyWaitingHandler;

    public MQProducers(Connection connection, MQQueueNames MQQueueNames) throws IOException {
        this.MQQueueNames = MQQueueNames;
        replyWaitingHandler = new ReplyWaitingHandler();
        querySendQueue = connection.createChannel();

        Channel queryReplyQueue = connection.createChannel();
        queryReplyQueue.exchangeDeclare(MQQueueNames.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);

        queryReplyQueue.queueDeclare(MQQueueNames.getQueryReplyQueue(), false, true, true, null);
        queryReplyQueue.queueBind(MQQueueNames.getQueryReplyQueue(), MQQueueNames.getExchange(), MQQueueNames.getQueryReplyQueue());
        queryReplyQueue.basicConsume(MQQueueNames.getQueryReplyQueue(), true, replyWaitingHandler::handleReplies, consumerTag -> {});
    }

    public Object queryServiceRequest(UUID id) {
        log.debug("queryServiceRequest: {}", id);
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(MQQueueNames.getQueryReplyQueue()).build();
        try {
            querySendQueue.basicPublish(MQQueueNames.getExchange(), MQQueueNames.getServicerequestQueryIdQueue(), props, SerializationUtils.serialize(id));
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
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(MQQueueNames.getQueryReplyQueue()).build();
        try {
            querySendQueue.basicPublish(MQQueueNames.getExchange(), MQQueueNames.getAccountQueryAccountIdQueue(), props, SerializationUtils.serialize(id));
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
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(MQQueueNames.getQueryReplyQueue()).build();
        try {
            querySendQueue.basicPublish(MQQueueNames.getExchange(), MQQueueNames.getAccountQueryLoanIdQueue(), props, SerializationUtils.serialize(id));
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
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(MQQueueNames.getQueryReplyQueue()).build();
        try {
            querySendQueue.basicPublish(MQQueueNames.getExchange(), MQQueueNames.getStatementQueryStatementQueue(), props, SerializationUtils.serialize(id));
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
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(MQQueueNames.getQueryReplyQueue()).build();
        try {
            querySendQueue.basicPublish(MQQueueNames.getExchange(), MQQueueNames.getStatementQueryStatementsQueue(), props, SerializationUtils.serialize(id));
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
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(MQQueueNames.getQueryReplyQueue()).build();
        try {
            querySendQueue.basicPublish(MQQueueNames.getExchange(), MQQueueNames.getServiceRequestCheckRequestQueue(), props, SerializationUtils.serialize(new byte[0]));
            return replyWaitingHandler.getReply(responseKey);
        } catch (IOException | InterruptedException e) {
            log.error("queryCheckRequest", e);
            throw new RuntimeException(e);
        }
    }
}
