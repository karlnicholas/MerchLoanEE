package com.github.karlnicholas.merchloan.businessdate.businessdate.message;

import com.github.karlnicholas.merchloan.jms.config.MQQueueNames;
import com.github.karlnicholas.merchloan.jmsmessage.BillingCycle;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class MQProducers {
    private final MQQueueNames MQQueueNames;
    private final Channel businessDateSendChannel;
    private final ConcurrentMap<String, Object> repliesWaiting;
    private static final int responseTimeout = 10000;
    private static final String emptyString = "";
    @Autowired
    public MQProducers(Connection connection, MQQueueNames MQQueueNames) throws IOException, TimeoutException {
        this.MQQueueNames = MQQueueNames;
        repliesWaiting = new ConcurrentHashMap<>();

        businessDateSendChannel = connection.createChannel();

        Channel businessDateResponseChannel = connection.createChannel();
        businessDateResponseChannel.exchangeDeclare(MQQueueNames.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);

        businessDateResponseChannel.queueDeclare(MQQueueNames.getBusinessDateReplyQueue(), false, true, true, null);
        businessDateResponseChannel.queueBind(MQQueueNames.getBusinessDateReplyQueue(), MQQueueNames.getExchange(), MQQueueNames.getBusinessDateReplyQueue());
        businessDateResponseChannel.basicConsume(MQQueueNames.getBusinessDateReplyQueue(), true, this::handleReplyQueue, consumerTag -> {});
    }

    public Object servicerequestCheckRequest() throws IOException, InterruptedException {
        log.debug("servicerequestCheckRequest:");
        String responseKey = UUID.randomUUID().toString();
        repliesWaiting.put(responseKey, emptyString);
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(MQQueueNames.getBusinessDateReplyQueue()).build();
        businessDateSendChannel.basicPublish(MQQueueNames.getExchange(), MQQueueNames.getServiceRequestCheckRequestQueue(), props, SerializationUtils.serialize(new byte[0]));
        synchronized (repliesWaiting) {
            while ( repliesWaiting.containsKey(responseKey) && repliesWaiting.get(responseKey) == emptyString ) {
                repliesWaiting.wait(responseTimeout);
            }
            return repliesWaiting.remove(responseKey);
        }
    }

    public Object acccountQueryLoansToCycle(LocalDate businessDate) throws IOException, InterruptedException {
        log.debug("acccountQueryLoansToCycle: {}", businessDate);
        String responseKey = UUID.randomUUID().toString();
        repliesWaiting.put(responseKey, emptyString);
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(MQQueueNames.getBusinessDateReplyQueue()).build();
        businessDateSendChannel.basicPublish(MQQueueNames.getExchange(), MQQueueNames.getAccountQueryLoansToCycleQueue(), props, SerializationUtils.serialize(businessDate));
        synchronized (repliesWaiting) {
            while ( repliesWaiting.containsKey(responseKey) && repliesWaiting.get(responseKey) == emptyString ) {
                repliesWaiting.wait(responseTimeout);
            }
            return repliesWaiting.remove(responseKey);
        }
    }

    public void serviceRequestBillLoan(BillingCycle billingCycle) throws IOException {
        businessDateSendChannel.basicPublish(MQQueueNames.getExchange(), MQQueueNames.getServiceRequestBillLoanQueue(), null, SerializationUtils.serialize(billingCycle));
    }

    private void handleReplyQueue(String consumerTag, Delivery delivery) {
        synchronized (repliesWaiting) {
            String corrId = delivery.getProperties().getCorrelationId();
            repliesWaiting.put(corrId, SerializationUtils.deserialize(delivery.getBody()));
            repliesWaiting.notifyAll();
        }
    }

}
