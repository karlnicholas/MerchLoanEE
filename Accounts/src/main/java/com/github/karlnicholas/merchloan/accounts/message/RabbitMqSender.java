package com.github.karlnicholas.merchloan.accounts.message;

import com.github.karlnicholas.merchloan.jms.config.RabbitMqProperties;
import com.github.karlnicholas.merchloan.jmsmessage.ServiceRequestResponse;
import com.github.karlnicholas.merchloan.jmsmessage.StatementHeader;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class RabbitMqSender {
    private final RabbitMqProperties rabbitMqProperties;
    private final Channel accountSendChannel;
    private final Channel accountReplyChannel;
    private final Map<String, Object> repliesWaiting;
    private static final int responseTimeout = 10000;

    @Autowired
    public RabbitMqSender(ConnectionFactory connectionFactory, RabbitMqProperties rabbitMqProperties) throws IOException, TimeoutException {
        this.rabbitMqProperties = rabbitMqProperties;
        repliesWaiting = new TreeMap<>();

        Connection connection = connectionFactory.newConnection();
        accountSendChannel = connection.createChannel();

//        accountSendChannel.queueDeclare(rabbitMqProperties.getServicerequestQueue(), false, true, true, null);
//        accountSendChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
//        accountSendChannel.queueBind(rabbitMqProperties.getServicerequestQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getServicerequestQueue());
//
//        accountSendChannel.queueDeclare(rabbitMqProperties.getStatementCloseStatementQueue(), false, true, true, null);
//        accountSendChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
//        accountSendChannel.queueBind(rabbitMqProperties.getStatementCloseStatementQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getStatementCloseStatementQueue());
//
//        accountSendChannel.queueDeclare(rabbitMqProperties.getStatementQueryMostRecentStatementQueue(), false, true, true, null);
//        accountSendChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
//        accountSendChannel.queueBind(rabbitMqProperties.getStatementQueryMostRecentStatementQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getStatementQueryMostRecentStatementQueue());

        connection = connectionFactory.newConnection();
        accountReplyChannel = connection.createChannel();

        accountReplyChannel.queueDeclare(rabbitMqProperties.getAccountReplyQueue(), false, true, true, null);
        accountReplyChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        accountReplyChannel.queueBind(rabbitMqProperties.getAccountReplyQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountReplyQueue());
        accountReplyChannel.basicConsume(rabbitMqProperties.getAccountReplyQueue(), true, this::handleReplyQueue, consumerTag->{});
    }

    public void serviceRequestServiceRequest(ServiceRequestResponse serviceRequest) throws IOException {
        log.debug("serviceRequestServiceRequest: {}", serviceRequest);
        accountSendChannel.basicPublish(rabbitMqProperties.getExchange(), rabbitMqProperties.getServicerequestQueue(), null, SerializationUtils.serialize(serviceRequest));
    }

    public void statementCloseStatement(StatementHeader statementHeader) throws IOException {
        log.debug("statementCloseStatement: {}", statementHeader);
        accountSendChannel.basicPublish(rabbitMqProperties.getExchange(), rabbitMqProperties.getStatementCloseStatementQueue(), null, SerializationUtils.serialize(statementHeader));
    }

    public Object queryMostRecentStatement(UUID loanId) throws IOException, InterruptedException {
        log.debug("queryMostRecentStatement: {}", loanId);
        String responseKey = UUID.randomUUID().toString();
        repliesWaiting.put(responseKey, null);
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(rabbitMqProperties.getAccountReplyQueue()).build();
        accountSendChannel.basicPublish(rabbitMqProperties.getExchange(), rabbitMqProperties.getStatementQueryMostRecentStatementQueue(), props, SerializationUtils.serialize(loanId));
        synchronized (repliesWaiting) {
            while ( repliesWaiting.get(responseKey) == null ) {
                repliesWaiting.wait(responseTimeout);
            }
            return repliesWaiting.remove(responseKey);
        }
    }

    private void handleReplyQueue(String consumerTag, Delivery delivery) {
        synchronized (repliesWaiting) {
            String corrId = delivery.getProperties().getCorrelationId();
            repliesWaiting.put(corrId, SerializationUtils.deserialize(delivery.getBody()));
            repliesWaiting.notifyAll();
        }
    }
}
