package com.github.karlnicholas.merchloan.statement.message;

import com.github.karlnicholas.merchloan.jms.config.MQQueueNames;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeoutException;

@Component
@Slf4j
public class MQProducers {
    private final MQQueueNames MQQueueNames;
    private final Channel statementSendChannel;
    private final ConcurrentMap<String, Object> repliesWaiting;
    private static final int responseTimeout = 10000;
    private static final String emptyString = "";

    @Autowired
    public MQProducers(Connection connection, MQQueueNames MQQueueNames) throws IOException, TimeoutException {
        this.MQQueueNames = MQQueueNames;
        repliesWaiting = new ConcurrentHashMap<>();
        statementSendChannel = connection.createChannel();

        Channel statementReplyQueue = connection.createChannel();
        statementReplyQueue.exchangeDeclare(MQQueueNames.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        statementReplyQueue.queueDeclare(MQQueueNames.getStatementReplyQueue(), false, true, true, null);
        statementReplyQueue.queueBind(MQQueueNames.getStatementReplyQueue(), MQQueueNames.getExchange(), MQQueueNames.getStatementReplyQueue());
        statementReplyQueue.basicConsume(MQQueueNames.getStatementReplyQueue(), true, this::handleReplyQueue, consumerTag -> {});
    }

    public Object accountBillingCycleCharge(BillingCycleCharge billingCycleCharge) throws IOException, InterruptedException {
        log.debug("accountBillingCycleCharge: {}", billingCycleCharge);
        String responseKey = billingCycleCharge.getId().toString();
        repliesWaiting.put(responseKey, emptyString);
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(MQQueueNames.getStatementReplyQueue()).build();
        statementSendChannel.basicPublish(MQQueueNames.getExchange(), MQQueueNames.getAccountBillingCycleChargeQueue(), props, SerializationUtils.serialize(billingCycleCharge));
        synchronized (repliesWaiting) {
            while ( repliesWaiting.containsKey(responseKey) && repliesWaiting.get(responseKey) == emptyString ) {
                repliesWaiting.wait(responseTimeout);
            }
            return repliesWaiting.remove(responseKey);
        }
    }

    public Object accountQueryStatementHeader(StatementHeader statementHeader) throws IOException, InterruptedException {
        log.debug("accountQueryStatementHeader: {}", statementHeader);
        String responseKey = statementHeader.getId().toString();
        repliesWaiting.put(responseKey, emptyString);
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(MQQueueNames.getStatementReplyQueue()).build();
        statementSendChannel.basicPublish(MQQueueNames.getExchange(), MQQueueNames.getAccountQueryStatementHeaderQueue(), props, SerializationUtils.serialize(statementHeader));
        synchronized (repliesWaiting) {
            while ( repliesWaiting.containsKey(responseKey) && repliesWaiting.get(responseKey) == emptyString ) {
                repliesWaiting.wait(responseTimeout);
            }
            return repliesWaiting.remove(responseKey);
        }
    }

    public void serviceRequestServiceRequest(ServiceRequestResponse serviceRequest) throws IOException {
        log.debug("serviceRequestServiceRequest: {}", serviceRequest);
        statementSendChannel.basicPublish(MQQueueNames.getExchange(), MQQueueNames.getServicerequestQueue(), null, SerializationUtils.serialize(serviceRequest));
    }

    public void accountLoanClosed(StatementHeader statementHeader) throws IOException {
        log.debug("accountLoanClosed: {}", statementHeader);
        statementSendChannel.basicPublish(MQQueueNames.getExchange(), MQQueueNames.getAccountLoanClosedQueue(), null, SerializationUtils.serialize(statementHeader));
    }

    public void serviceRequestStatementComplete(StatementCompleteResponse requestResponse) throws IOException {
        log.debug("serviceRequestStatementComplete: {}", requestResponse);
        statementSendChannel.basicPublish(MQQueueNames.getExchange(), MQQueueNames.getServiceRequestStatementCompleteQueue(), null, SerializationUtils.serialize(requestResponse));
    }

    private void handleReplyQueue(String consumerTag, Delivery delivery) {
        synchronized (repliesWaiting) {
            String corrId = delivery.getProperties().getCorrelationId();
            repliesWaiting.put(corrId, SerializationUtils.deserialize(delivery.getBody()));
            repliesWaiting.notifyAll();
        }
    }

}
