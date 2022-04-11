package com.github.karlnicholas.merchloan.query.message;

import com.github.karlnicholas.merchloan.jms.config.RabbitMqProperties;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class RabbitMqSender {
    private final RabbitMqProperties rabbitMqProperties;
    private final Channel querySendQueue;
    private final ConcurrentMap<String, ResponseClass> repliesWaiting;
    private static final String emptyString = "";
    private static final int responseTimeout = 10000;

    public RabbitMqSender(ConnectionFactory connectionFactory, RabbitMqProperties rabbitMqProperties) throws IOException, TimeoutException, InterruptedException {
        this.rabbitMqProperties = rabbitMqProperties;
        repliesWaiting = new ConcurrentHashMap<>();
        Connection connection = connectionFactory.newConnection();
        querySendQueue = connection.createChannel();

        connection = connectionFactory.newConnection();
        Channel queryReplyQueue = connection.createChannel();
        queryReplyQueue.queueDeclare(rabbitMqProperties.getQueryReplyQueue(), false, true, true, null);
        queryReplyQueue.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        queryReplyQueue.queueBind(rabbitMqProperties.getQueryReplyQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getQueryReplyQueue());
        queryReplyQueue.basicConsume(rabbitMqProperties.getQueryReplyQueue(), true, this::handleReplyQueue, consumerTag -> {});
    }

    public Object queryServiceRequest(UUID id) {
        log.debug("queryServiceRequest: {}", id);
        String responseKey = id.toString();
        repliesWaiting.put(responseKey, ResponseClass.builder().type("queryServiceRequest").build());
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(rabbitMqProperties.getQueryReplyQueue()).build();
        try {
            querySendQueue.basicPublish(rabbitMqProperties.getExchange(), rabbitMqProperties.getServicerequestQueryIdQueue(), props, SerializationUtils.serialize(id));
            synchronized (repliesWaiting) {
                while ( repliesWaiting.containsKey(responseKey) && repliesWaiting.get(responseKey).getResponse() == null ) {
                    repliesWaiting.wait(responseTimeout);
                }
                return repliesWaiting.remove(responseKey).getResponse();
            }
        } catch (IOException | InterruptedException e) {
            log.error("queryServiceRequest", e);
            throw new RuntimeException(e);
        }
    }

    public Object queryAccount(UUID id) {
        log.debug("queryAccount: {}", id);
        String responseKey = id.toString();
        repliesWaiting.put(responseKey, ResponseClass.builder().type("queryAccount").build());
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(rabbitMqProperties.getQueryReplyQueue()).build();
        try {
            querySendQueue.basicPublish(rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountQueryAccountIdQueue(), props, SerializationUtils.serialize(id));
            synchronized (repliesWaiting) {
                while ( repliesWaiting.containsKey(responseKey) && repliesWaiting.get(responseKey).getResponse() == null ) {
                    repliesWaiting.wait(responseTimeout);
                }
                return repliesWaiting.remove(responseKey).getResponse();
            }
        } catch (IOException | InterruptedException e) {
            log.error("queryAccount", e);
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public Object queryLoan(UUID id) {
        log.debug("queryLoan: {}", id);
        String responseKey = id.toString();
        repliesWaiting.put(responseKey, ResponseClass.builder().type("queryLoan").thread(Thread.currentThread().getName()).build());
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(rabbitMqProperties.getQueryReplyQueue()).build();
        try {
            querySendQueue.basicPublish(rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountQueryLoanIdQueue(), props, SerializationUtils.serialize(id));
            synchronized (repliesWaiting) {
                while ( repliesWaiting.containsKey(responseKey) && repliesWaiting.get(responseKey).getResponse() == null ) {
                    repliesWaiting.wait(responseTimeout);
                }
                return repliesWaiting.remove(responseKey).getResponse();
            }
        } catch (IOException | InterruptedException e) {
            log.error("queryLoan", e);
            throw new RuntimeException(e);
        }
    }

    public Object queryStatement(UUID id) {
        log.debug("queryStatement: {}", id);
        String responseKey = id.toString();
        repliesWaiting.put(responseKey, ResponseClass.builder().type("queryStatement").build());
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(rabbitMqProperties.getQueryReplyQueue()).build();
        try {
            querySendQueue.basicPublish(rabbitMqProperties.getExchange(), rabbitMqProperties.getStatementQueryStatementQueue(), props, SerializationUtils.serialize(id));
            synchronized (repliesWaiting) {
                while ( repliesWaiting.containsKey(responseKey) && repliesWaiting.get(responseKey).getResponse() == null ) {
                    repliesWaiting.wait(responseTimeout);
                }
                return repliesWaiting.remove(responseKey).getResponse();
            }
        } catch (IOException | InterruptedException e) {
            log.error("queryStatement", e);
            throw new RuntimeException(e);
        }
    }

    public Object queryStatements(UUID id) {
        log.debug("queryStatements: {}", id);
        String responseKey = id.toString();
        repliesWaiting.put(responseKey, ResponseClass.builder().type("queryStatements").build());
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(rabbitMqProperties.getQueryReplyQueue()).build();
        try {
            querySendQueue.basicPublish(rabbitMqProperties.getExchange(), rabbitMqProperties.getStatementQueryStatementsQueue(), props, SerializationUtils.serialize(id));
            synchronized (repliesWaiting) {
                while ( repliesWaiting.containsKey(responseKey) && repliesWaiting.get(responseKey).getResponse() == null ) {
                    repliesWaiting.wait(responseTimeout);
                }
                return repliesWaiting.remove(responseKey).getResponse();
            }
        } catch (IOException | InterruptedException e) {
            log.error("queryStatements", e);
            throw new RuntimeException(e);
        }
    }

    public Object queryCheckRequest() {
        log.debug("queryCheckRequest:");
        String responseKey = UUID.randomUUID().toString();
        repliesWaiting.put(responseKey, ResponseClass.builder().type("queryCheckRequest").build());
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(rabbitMqProperties.getQueryReplyQueue()).build();
        try {
            querySendQueue.basicPublish(rabbitMqProperties.getExchange(), rabbitMqProperties.getServiceRequestCheckRequestQueue(), props, SerializationUtils.serialize(new byte[0]));
            synchronized (repliesWaiting) {
                while ( repliesWaiting.containsKey(responseKey) && repliesWaiting.get(responseKey).getResponse() == null ) {
                    repliesWaiting.wait(responseTimeout);
                }
                return repliesWaiting.remove(responseKey).getResponse();
            }
        } catch (IOException | InterruptedException e) {
            log.error("queryCheckRequest", e);
            throw new RuntimeException(e);
        }
    }

    private void handleReplyQueue(String consumerTag, Delivery delivery) {
        synchronized (repliesWaiting) {
            String corrId = delivery.getProperties().getCorrelationId();
            repliesWaiting.get(corrId).setResponse(SerializationUtils.deserialize(delivery.getBody()));
            repliesWaiting.notifyAll();
        }
    }

}
