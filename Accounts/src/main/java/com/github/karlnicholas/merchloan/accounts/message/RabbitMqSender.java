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
    private final Channel serviceRequestChannel;
    private final Channel statementCloseStatementChannel;
    private final Channel accountReplyQueue;
    private final Channel statementQueryMostRecentStatementQueue;
    private final Map<String, Object> repliesWaiting;
    private static final int responseTimeout = 10000;

    @Autowired
    public RabbitMqSender(ConnectionFactory connectionFactory, RabbitMqProperties rabbitMqProperties) throws IOException, TimeoutException {
        this.rabbitMqProperties = rabbitMqProperties;
        repliesWaiting = new TreeMap<>();

        Connection connection = connectionFactory.newConnection();

        serviceRequestChannel = connection.createChannel();
        serviceRequestChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        serviceRequestChannel.queueDeclare(rabbitMqProperties.getServicerequestQueue(), false, false, false, null);
        serviceRequestChannel.exchangeBind(rabbitMqProperties.getServicerequestQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getServicerequestQueue());

        statementCloseStatementChannel = connection.createChannel();
        statementCloseStatementChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        statementCloseStatementChannel.queueDeclare(rabbitMqProperties.getStatementCloseStatementQueue(), false, false, false, null);
        statementCloseStatementChannel.exchangeBind(rabbitMqProperties.getStatementCloseStatementQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getStatementCloseStatementQueue());

        statementQueryMostRecentStatementQueue = connection.createChannel();
        statementQueryMostRecentStatementQueue.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        statementQueryMostRecentStatementQueue.queueDeclare(rabbitMqProperties.getStatementQueryMostRecentStatementQueue(), false, false, false, null);
        statementQueryMostRecentStatementQueue.exchangeBind(rabbitMqProperties.getStatementQueryMostRecentStatementQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getStatementQueryMostRecentStatementQueue());

        connection = connectionFactory.newConnection();

        accountReplyQueue = connection.createChannel();
        accountReplyQueue.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        accountReplyQueue.queueDeclare(rabbitMqProperties.getAccountReplyQueue(), false, false, false, null);
        accountReplyQueue.exchangeBind(rabbitMqProperties.getAccountReplyQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountReplyQueue());
        accountReplyQueue.basicConsume(rabbitMqProperties.getAccountReplyQueue(), true, this::handleReplyQueue, consumerTag->{});
    }

    public void serviceRequestServiceRequest(ServiceRequestResponse serviceRequest) throws IOException {
        log.debug("serviceRequestServiceRequest: {}", serviceRequest);
        serviceRequestChannel.basicPublish(rabbitMqProperties.getExchange(), rabbitMqProperties.getServicerequestQueue(), null, SerializationUtils.serialize(serviceRequest));
    }

    public void statementCloseStatement(StatementHeader statementHeader) throws IOException {
        log.debug("statementCloseStatement: {}", statementHeader);
        statementCloseStatementChannel.basicPublish(rabbitMqProperties.getExchange(), rabbitMqProperties.getStatementCloseStatementQueue(), null, SerializationUtils.serialize(statementHeader));
    }

    public Object queryMostRecentStatement(UUID loanId) throws IOException, InterruptedException {
        log.debug("queryMostRecentStatement: {}", loanId);
        String responseKey = UUID.randomUUID().toString();
        repliesWaiting.put(responseKey, null);
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(rabbitMqProperties.getAccountReplyQueue()).build();
        statementQueryMostRecentStatementQueue.basicPublish(rabbitMqProperties.getExchange(), rabbitMqProperties.getStatementQueryMostRecentStatementQueue(), props, SerializationUtils.serialize(loanId));
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
