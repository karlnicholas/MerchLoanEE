package com.github.karlnicholas.merchloan.accounts.message;

import com.github.karlnicholas.merchloan.jms.config.MQQueueNames;
import com.github.karlnicholas.merchloan.jmsmessage.ServiceRequestResponse;
import com.github.karlnicholas.merchloan.jmsmessage.StatementHeader;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class MQProducers {
    private final MQQueueNames mqQueueNames;
    private final Channel accountSendChannel;
    private final ConcurrentMap<String, Object> repliesWaiting;
    private static final int responseTimeout = 10000;
    private static final String emptyString = "";

    @Autowired
    public MQProducers(Connection connection, MQQueueNames mqQueueNames) throws IOException, TimeoutException {
        this.mqQueueNames = mqQueueNames;
        repliesWaiting = new ConcurrentHashMap<>();

        accountSendChannel = connection.createChannel();

        Channel accountReplyChannel = connection.createChannel();
        accountReplyChannel.queueDeclare(mqQueueNames.getAccountReplyQueue(), false, true, true, null);

        accountReplyChannel.exchangeDeclare(mqQueueNames.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        accountReplyChannel.queueBind(mqQueueNames.getAccountReplyQueue(), mqQueueNames.getExchange(), mqQueueNames.getAccountReplyQueue());
        accountReplyChannel.basicConsume(mqQueueNames.getAccountReplyQueue(), true, this::handleReplyQueue, consumerTag->{});
    }

    public void serviceRequestServiceRequest(ServiceRequestResponse serviceRequest) throws IOException {
        log.debug("serviceRequestServiceRequest: {}", serviceRequest);
        accountSendChannel.basicPublish(mqQueueNames.getExchange(), mqQueueNames.getServicerequestQueue(), null, SerializationUtils.serialize(serviceRequest));
    }

    public void statementCloseStatement(StatementHeader statementHeader) throws IOException {
        log.debug("statementCloseStatement: {}", statementHeader);
        accountSendChannel.basicPublish(mqQueueNames.getExchange(), mqQueueNames.getStatementCloseStatementQueue(), null, SerializationUtils.serialize(statementHeader));
    }

    public Object queryMostRecentStatement(UUID loanId) throws IOException, InterruptedException {
        log.debug("queryMostRecentStatement: {}", loanId);
        String responseKey = loanId.toString();
        repliesWaiting.put(responseKey, emptyString);
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(mqQueueNames.getAccountReplyQueue()).build();
        accountSendChannel.basicPublish(mqQueueNames.getExchange(), mqQueueNames.getStatementQueryMostRecentStatementQueue(), props, SerializationUtils.serialize(loanId));
        synchronized (repliesWaiting) {
            while ( repliesWaiting.containsKey(responseKey) && repliesWaiting.get(responseKey) == emptyString ) {
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
