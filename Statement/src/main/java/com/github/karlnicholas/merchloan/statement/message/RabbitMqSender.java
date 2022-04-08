package com.github.karlnicholas.merchloan.statement.message;

import com.github.karlnicholas.merchloan.jms.config.RabbitMqProperties;
import com.github.karlnicholas.merchloan.jmsmessage.BillingCycleCharge;
import com.github.karlnicholas.merchloan.jmsmessage.ServiceRequestResponse;
import com.github.karlnicholas.merchloan.jmsmessage.StatementCompleteResponse;
import com.github.karlnicholas.merchloan.jmsmessage.StatementHeader;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Component
@Slf4j
public class RabbitMqSender {
    private final RabbitMqProperties rabbitMqProperties;
    private final Channel statementSendChannel;
    private final Map<String, Object> repliesWaiting;
    private static final int responseTimeout = 10000;

    @Autowired
    public RabbitMqSender(ConnectionFactory connectionFactory, RabbitMqProperties rabbitMqProperties) throws IOException, TimeoutException {
        this.rabbitMqProperties = rabbitMqProperties;
        repliesWaiting = new TreeMap<>();
        Connection connection = connectionFactory.newConnection();
        statementSendChannel = connection.createChannel();

        connection = connectionFactory.newConnection();
        Channel statementReplyQueue = connection.createChannel();
        statementReplyQueue.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);

        statementReplyQueue.queueDeclare(rabbitMqProperties.getStatementReplyQueue(), false, true, true, null);
        statementReplyQueue.queueBind(rabbitMqProperties.getStatementReplyQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getStatementReplyQueue());
        statementReplyQueue.basicConsume(rabbitMqProperties.getStatementReplyQueue(), true, this::handleReplyQueue, consumerTag -> {});
    }

    public Object accountBillingCycleCharge(BillingCycleCharge billingCycleCharge) throws IOException, InterruptedException {
        log.debug("accountBillingCycleCharge: {}", billingCycleCharge);
        String responseKey = UUID.randomUUID().toString();
        repliesWaiting.put(responseKey, null);
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(rabbitMqProperties.getStatementReplyQueue()).build();
        statementSendChannel.basicPublish(rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountBillingCycleChargeQueue(), props, SerializationUtils.serialize(billingCycleCharge));
        synchronized (repliesWaiting) {
            while ( repliesWaiting.get(responseKey) == null ) {
                repliesWaiting.wait(responseTimeout);
            }
            return repliesWaiting.remove(responseKey);
        }
    }

    public Object accountQueryStatementHeader(StatementHeader statementHeader) throws IOException, InterruptedException {
        log.debug("accountQueryStatementHeader: {}", statementHeader);
        String responseKey = UUID.randomUUID().toString();
        repliesWaiting.put(responseKey, null);
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(rabbitMqProperties.getStatementReplyQueue()).build();
        statementSendChannel.basicPublish(rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountQueryStatementHeaderQueue(), props, SerializationUtils.serialize(statementHeader));
        synchronized (repliesWaiting) {
            while ( repliesWaiting.get(responseKey) == null ) {
                repliesWaiting.wait(responseTimeout);
            }
            return repliesWaiting.remove(responseKey);
        }
    }

    public void serviceRequestServiceRequest(ServiceRequestResponse serviceRequest) throws IOException {
        log.debug("serviceRequestServiceRequest: {}", serviceRequest);
        statementSendChannel.basicPublish(rabbitMqProperties.getExchange(), rabbitMqProperties.getServicerequestQueue(), null, SerializationUtils.serialize(serviceRequest));
    }

    public void accountLoanClosed(StatementHeader statementHeader) throws IOException {
        log.debug("accountLoanClosed: {}", statementHeader);
        statementSendChannel.basicPublish(rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountLoanClosedQueue(), null, SerializationUtils.serialize(statementHeader));
    }

    public void serviceRequestStatementComplete(StatementCompleteResponse requestResponse) throws IOException {
        log.debug("serviceRequestStatementComplete: {}", requestResponse);
        statementSendChannel.basicPublish(rabbitMqProperties.getExchange(), rabbitMqProperties.getServiceRequestStatementCompleteQueue(), null, SerializationUtils.serialize(requestResponse));
    }

    private void handleReplyQueue(String consumerTag, Delivery delivery) {
        synchronized (repliesWaiting) {
            String corrId = delivery.getProperties().getCorrelationId();
            repliesWaiting.put(corrId, SerializationUtils.deserialize(delivery.getBody()));
            repliesWaiting.notifyAll();
        }
    }

}
