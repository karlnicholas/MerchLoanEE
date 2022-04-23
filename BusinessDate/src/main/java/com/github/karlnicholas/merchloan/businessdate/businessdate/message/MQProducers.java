package com.github.karlnicholas.merchloan.businessdate.businessdate.message;

import com.github.karlnicholas.merchloan.jms.ReplyWaiting;
import com.github.karlnicholas.merchloan.jms.config.MQQueueNames;
import com.github.karlnicholas.merchloan.jmsmessage.BillingCycle;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class MQProducers {
    private final MQQueueNames MQQueueNames;
    private final Channel businessDateSendChannel;
    private final Map<String, ReplyWaiting> repliesWaiting;
    @Autowired
    public MQProducers(Connection connection, MQQueueNames MQQueueNames) throws IOException, TimeoutException {
        this.MQQueueNames = MQQueueNames;
        repliesWaiting = new TreeMap<>();

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
        repliesWaiting.put(responseKey, ReplyWaiting.builder().nonoTime(System.nanoTime()).reply(null).build());
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(MQQueueNames.getBusinessDateReplyQueue()).build();
        businessDateSendChannel.basicPublish(MQQueueNames.getExchange(), MQQueueNames.getServiceRequestCheckRequestQueue(), props, SerializationUtils.serialize(new byte[0]));
        synchronized (repliesWaiting) {
            while ( repliesWaiting.containsKey(responseKey) && repliesWaiting.get(responseKey).checkReply().isEmpty()) {
                repliesWaiting.wait(ReplyWaiting.responseTimeout);
                if ( System.nanoTime() - repliesWaiting.get(responseKey).getNonoTime() > ReplyWaiting.timeoutMax) {
                    log.error("servicerequestCheckRequest reply timeout");
                    break;
                }
            }
            return repliesWaiting.remove(responseKey).getReply();
        }
    }

    public Object acccountQueryLoansToCycle(LocalDate businessDate) throws IOException, InterruptedException {
        log.debug("acccountQueryLoansToCycle: {}", businessDate);
        String responseKey = UUID.randomUUID().toString();
        repliesWaiting.put(responseKey, ReplyWaiting.builder().nonoTime(System.nanoTime()).reply(null).build());
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(MQQueueNames.getBusinessDateReplyQueue()).build();
        businessDateSendChannel.basicPublish(MQQueueNames.getExchange(), MQQueueNames.getAccountQueryLoansToCycleQueue(), props, SerializationUtils.serialize(businessDate));
        synchronized (repliesWaiting) {
            while ( repliesWaiting.containsKey(responseKey) && repliesWaiting.get(responseKey).checkReply().isEmpty()) {
                repliesWaiting.wait(ReplyWaiting.responseTimeout);
                if ( System.nanoTime() - repliesWaiting.get(responseKey).getNonoTime() > ReplyWaiting.timeoutMax) {
                    log.error("servicerequestCheckRequest reply timeout");
                    break;
                }
            }
            return repliesWaiting.remove(responseKey).getReply();
        }
    }

    public void serviceRequestBillLoan(BillingCycle billingCycle) throws IOException {
        businessDateSendChannel.basicPublish(MQQueueNames.getExchange(), MQQueueNames.getServiceRequestBillLoanQueue(), null, SerializationUtils.serialize(billingCycle));
    }

    private void handleReplyQueue(String consumerTag, Delivery delivery) {
        synchronized (repliesWaiting) {
            String corrId = delivery.getProperties().getCorrelationId();
            repliesWaiting.get(corrId).setReply(SerializationUtils.deserialize(delivery.getBody()));
            repliesWaiting.notifyAll();
        }
    }

}
