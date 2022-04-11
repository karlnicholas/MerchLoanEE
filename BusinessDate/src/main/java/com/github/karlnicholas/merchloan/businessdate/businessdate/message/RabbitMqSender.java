package com.github.karlnicholas.merchloan.businessdate.businessdate.message;

import com.github.karlnicholas.merchloan.jms.config.RabbitMqProperties;
import com.github.karlnicholas.merchloan.jmsmessage.BillingCycle;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class RabbitMqSender {
    private final RabbitMqProperties rabbitMqProperties;
    private final Channel businessDateSendChannel;
    private final ConcurrentMap<String, Object> repliesWaiting;
    private static final int responseTimeout = 10000;
    private static final String emptyString = "";
    @Autowired
    public RabbitMqSender(Connection connection, RabbitMqProperties rabbitMqProperties) throws IOException, TimeoutException {
        this.rabbitMqProperties = rabbitMqProperties;
        repliesWaiting = new ConcurrentHashMap<>();

        businessDateSendChannel = connection.createChannel();

        Channel businessDateResponseChannel = connection.createChannel();
        businessDateResponseChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);

        businessDateResponseChannel.queueDeclare(rabbitMqProperties.getBusinessDateReplyQueue(), false, true, true, null);
        businessDateResponseChannel.queueBind(rabbitMqProperties.getBusinessDateReplyQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getBusinessDateReplyQueue());
        businessDateResponseChannel.basicConsume(rabbitMqProperties.getBusinessDateReplyQueue(), true, this::handleReplyQueue, consumerTag -> {});
    }

    public Object servicerequestCheckRequest() throws IOException, InterruptedException {
        log.debug("servicerequestCheckRequest:");
        String responseKey = UUID.randomUUID().toString();
        repliesWaiting.put(responseKey, emptyString);
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(rabbitMqProperties.getBusinessDateReplyQueue()).build();
        businessDateSendChannel.basicPublish(rabbitMqProperties.getExchange(), rabbitMqProperties.getServiceRequestCheckRequestQueue(), props, SerializationUtils.serialize(new byte[0]));
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
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(rabbitMqProperties.getBusinessDateReplyQueue()).build();
        businessDateSendChannel.basicPublish(rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountQueryLoansToCycleQueue(), props, SerializationUtils.serialize(businessDate));
        synchronized (repliesWaiting) {
            while ( repliesWaiting.containsKey(responseKey) && repliesWaiting.get(responseKey) == emptyString ) {
                repliesWaiting.wait(responseTimeout);
            }
            return repliesWaiting.remove(responseKey);
        }
    }

    public void serviceRequestBillLoan(BillingCycle billingCycle) throws IOException {
        businessDateSendChannel.basicPublish(rabbitMqProperties.getExchange(), rabbitMqProperties.getServiceRequestBillLoanQueue(), null, SerializationUtils.serialize(billingCycle));
    }

    private void handleReplyQueue(String consumerTag, Delivery delivery) {
        synchronized (repliesWaiting) {
            String corrId = delivery.getProperties().getCorrelationId();
            repliesWaiting.put(corrId, SerializationUtils.deserialize(delivery.getBody()));
            repliesWaiting.notifyAll();
        }
    }

}
