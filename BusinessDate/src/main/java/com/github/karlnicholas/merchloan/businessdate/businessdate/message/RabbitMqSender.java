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
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class RabbitMqSender {
    private RabbitMqProperties rabbitMqProperties;
    private final Channel serviceRequestCheckRequestQueue;
    private final Channel accountQueryLoansToCycleQueue;
    private final Channel serviceRequestBillLoanQueue;
    private final Map<String, Object> repliesWaiting;
    private static final int responseTimeout = 10000;
    @Autowired
    public RabbitMqSender(ConnectionFactory connectionFactory, RabbitMqProperties rabbitMqProperties) throws IOException, TimeoutException {
        this.rabbitMqProperties = rabbitMqProperties;
        repliesWaiting = new TreeMap<>();
        Connection connection = connectionFactory.newConnection();

        serviceRequestCheckRequestQueue = connection.createChannel();
        serviceRequestCheckRequestQueue.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        serviceRequestCheckRequestQueue.queueDeclare(rabbitMqProperties.getServiceRequestCheckRequestQueue(), false, false, false, null);
        serviceRequestCheckRequestQueue.exchangeBind(rabbitMqProperties.getServiceRequestCheckRequestQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getServiceRequestCheckRequestQueue());

        accountQueryLoansToCycleQueue = connection.createChannel();
        accountQueryLoansToCycleQueue.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        accountQueryLoansToCycleQueue.queueDeclare(rabbitMqProperties.getAccountQueryLoansToCycleQueue(), false, false, false, null);
        accountQueryLoansToCycleQueue.exchangeBind(rabbitMqProperties.getAccountQueryLoansToCycleQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountQueryLoansToCycleQueue());

        serviceRequestBillLoanQueue = connection.createChannel();
        serviceRequestBillLoanQueue.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        serviceRequestBillLoanQueue.queueDeclare(rabbitMqProperties.getServiceRequestBillLoanQueue(), false, false, false, null);
        serviceRequestBillLoanQueue.exchangeBind(rabbitMqProperties.getServiceRequestBillLoanQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getServiceRequestBillLoanQueue());

        connection = connectionFactory.newConnection();
        Channel businessDateReplyQueue = connection.createChannel();
        businessDateReplyQueue.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        businessDateReplyQueue.queueDeclare(rabbitMqProperties.getBusinessDateReplyQueue(), false, false, false, null);
        businessDateReplyQueue.exchangeBind(rabbitMqProperties.getBusinessDateReplyQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getBusinessDateReplyQueue());
        businessDateReplyQueue.basicConsume(rabbitMqProperties.getBusinessDateReplyQueue(), true, this::handleReplyQueue, consumerTag -> {});
    }

    public Object servicerequestCheckRequest() throws IOException, InterruptedException {
        log.debug("servicerequestCheckRequest:");
        String responseKey = UUID.randomUUID().toString();
        repliesWaiting.put(responseKey, null);
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(rabbitMqProperties.getBusinessDateReplyQueue()).build();
        serviceRequestCheckRequestQueue.basicPublish(rabbitMqProperties.getExchange(), rabbitMqProperties.getServiceRequestCheckRequestQueue(), props, SerializationUtils.serialize(new byte[0]));
        synchronized (repliesWaiting) {
            while ( repliesWaiting.get(responseKey) == null ) {
                repliesWaiting.wait(responseTimeout);
            }
            return repliesWaiting.remove(responseKey);
        }
    }

    public Object acccountQueryLoansToCycle(LocalDate businessDate) throws IOException, InterruptedException {
        log.debug("acccountQueryLoansToCycle: {}", businessDate);
        String responseKey = UUID.randomUUID().toString();
        repliesWaiting.put(responseKey, null);
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(rabbitMqProperties.getBusinessDateReplyQueue()).build();
        accountQueryLoansToCycleQueue.basicPublish(rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountQueryLoansToCycleQueue(), props, SerializationUtils.serialize(businessDate));
        synchronized (repliesWaiting) {
            while ( repliesWaiting.get(responseKey) == null ) {
                repliesWaiting.wait(responseTimeout);
            }
            return repliesWaiting.remove(responseKey);
        }
    }

    public void serviceRequestBillLoan(BillingCycle billingCycle) throws IOException {
        serviceRequestBillLoanQueue.basicPublish(rabbitMqProperties.getExchange(), rabbitMqProperties.getServiceRequestBillLoanQueue(), null, SerializationUtils.serialize(billingCycle));
    }

    private void handleReplyQueue(String consumerTag, Delivery delivery) {
        synchronized (repliesWaiting) {
            String corrId = delivery.getProperties().getCorrelationId();
            repliesWaiting.put(corrId, SerializationUtils.deserialize(delivery.getBody()));
            repliesWaiting.notifyAll();
        }
    }

}
