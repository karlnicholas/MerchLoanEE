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
    private final Channel accountBillingCycleChargeQueue;
    private final Channel accountQueryStatementHeaderQueue;
    private final Channel servicerequestQueue;
    private final Channel accountLoanClosedQueue;
    private final Channel serviceRequestStatementCompleteQueue;
    private final Map<String, Object> repliesWaiting;
    private static final int responseTimeout = 10000;

    @Autowired
    public RabbitMqSender(ConnectionFactory connectionFactory, RabbitMqProperties rabbitMqProperties) throws IOException, TimeoutException {
        this.rabbitMqProperties = rabbitMqProperties;
        repliesWaiting = new TreeMap<>();
        Connection connection = connectionFactory.newConnection();

        accountBillingCycleChargeQueue = connection.createChannel();
        accountBillingCycleChargeQueue.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        accountBillingCycleChargeQueue.queueDeclare(rabbitMqProperties.getAccountBillingCycleChargeQueue(), false, false, false, null);
        accountBillingCycleChargeQueue.exchangeBind(rabbitMqProperties.getAccountBillingCycleChargeQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountBillingCycleChargeQueue());

        accountQueryStatementHeaderQueue = connection.createChannel();
        accountQueryStatementHeaderQueue.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        accountQueryStatementHeaderQueue.queueDeclare(rabbitMqProperties.getAccountQueryStatementHeaderQueue(), false, false, false, null);
        accountQueryStatementHeaderQueue.exchangeBind(rabbitMqProperties.getAccountQueryStatementHeaderQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountQueryStatementHeaderQueue());

        servicerequestQueue = connection.createChannel();
        servicerequestQueue.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        servicerequestQueue.queueDeclare(rabbitMqProperties.getServicerequestQueue(), false, false, false, null);
        servicerequestQueue.exchangeBind(rabbitMqProperties.getServicerequestQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getServicerequestQueue());

        accountLoanClosedQueue = connection.createChannel();
        accountLoanClosedQueue.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        accountLoanClosedQueue.queueDeclare(rabbitMqProperties.getAccountLoanClosedQueue(), false, false, false, null);
        accountLoanClosedQueue.exchangeBind(rabbitMqProperties.getAccountLoanClosedQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountLoanClosedQueue());

        serviceRequestStatementCompleteQueue = connection.createChannel();
        serviceRequestStatementCompleteQueue.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        serviceRequestStatementCompleteQueue.queueDeclare(rabbitMqProperties.getServiceRequestStatementCompleteQueue(), false, false, false, null);
        serviceRequestStatementCompleteQueue.exchangeBind(rabbitMqProperties.getServiceRequestStatementCompleteQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getServiceRequestStatementCompleteQueue());

        connection = connectionFactory.newConnection();
        Channel statementReplyQueue = connection.createChannel();
        statementReplyQueue.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        statementReplyQueue.queueDeclare(rabbitMqProperties.getStatementReplyQueue(), false, false, false, null);
        statementReplyQueue.exchangeBind(rabbitMqProperties.getStatementReplyQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getStatementReplyQueue());
        statementReplyQueue.basicConsume(rabbitMqProperties.getStatementReplyQueue(), true, this::handleReplyQueue, consumerTag -> {});
    }

    public Object accountBillingCycleCharge(BillingCycleCharge billingCycleCharge) throws IOException, InterruptedException {
        log.debug("accountBillingCycleCharge: {}", billingCycleCharge);
        String responseKey = UUID.randomUUID().toString();
        repliesWaiting.put(responseKey, null);
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(rabbitMqProperties.getStatementReplyQueue()).build();
        accountBillingCycleChargeQueue.basicPublish(rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountBillingCycleChargeQueue(), props, SerializationUtils.serialize(billingCycleCharge));
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
        accountQueryStatementHeaderQueue.basicPublish(rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountQueryStatementHeaderQueue(), props, SerializationUtils.serialize(statementHeader));
        synchronized (repliesWaiting) {
            while ( repliesWaiting.get(responseKey) == null ) {
                repliesWaiting.wait(responseTimeout);
            }
            return repliesWaiting.remove(responseKey);
        }
    }

    public void serviceRequestServiceRequest(ServiceRequestResponse serviceRequest) throws IOException {
        log.debug("serviceRequestServiceRequest: {}", serviceRequest);
        servicerequestQueue.basicPublish(rabbitMqProperties.getExchange(), rabbitMqProperties.getServicerequestQueue(), null, SerializationUtils.serialize(serviceRequest));
    }

    public void accountLoanClosed(StatementHeader statementHeader) throws IOException {
        log.debug("accountLoanClosed: {}", statementHeader);
        accountLoanClosedQueue.basicPublish(rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountLoanClosedQueue(), null, SerializationUtils.serialize(statementHeader));
    }

    public void serviceRequestStatementComplete(StatementCompleteResponse requestResponse) throws IOException {
        log.debug("serviceRequestStatementComplete: {}", requestResponse);
        serviceRequestStatementCompleteQueue.basicPublish(rabbitMqProperties.getExchange(), rabbitMqProperties.getServiceRequestStatementCompleteQueue(), null, SerializationUtils.serialize(requestResponse));
    }

    private void handleReplyQueue(String consumerTag, Delivery delivery) {
        synchronized (repliesWaiting) {
            String corrId = delivery.getProperties().getCorrelationId();
            repliesWaiting.put(corrId, SerializationUtils.deserialize(delivery.getBody()));
            repliesWaiting.notifyAll();
        }
    }

}
