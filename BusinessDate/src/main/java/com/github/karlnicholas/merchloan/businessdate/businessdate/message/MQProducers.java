package com.github.karlnicholas.merchloan.businessdate.businessdate.message;

import com.github.karlnicholas.merchloan.jms.ReplyWaitingHandler;
import com.github.karlnicholas.merchloan.jms.config.MQQueueNames;
import com.github.karlnicholas.merchloan.jmsmessage.BillingCycle;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

@Service
@Slf4j
public class MQProducers {
    private final MQQueueNames mqQueueNames;
    private final Channel businessDateSendChannel;
    private final ReplyWaitingHandler replyWaitingHandler;
    @Autowired
    public MQProducers(Connection connection, MQQueueNames mqQueueNames) throws IOException {
        this.mqQueueNames = mqQueueNames;
        replyWaitingHandler = new ReplyWaitingHandler();

        businessDateSendChannel = connection.createChannel();
        mqQueueNames.bindConsumer(connection, mqQueueNames.getExchange(), mqQueueNames.getBusinessDateReplyQueue(), replyWaitingHandler::handleReplies);
    }

    public Object servicerequestCheckRequest() throws IOException, InterruptedException {
        log.debug("servicerequestCheckRequest:");
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(mqQueueNames.getBusinessDateReplyQueue()).build();
        businessDateSendChannel.basicPublish(mqQueueNames.getExchange(), mqQueueNames.getServiceRequestCheckRequestQueue(), props, SerializationUtils.serialize(new byte[0]));
        return replyWaitingHandler.getReply(responseKey);
    }

    public Object acccountQueryLoansToCycle(LocalDate businessDate) throws IOException, InterruptedException {
        log.debug("acccountQueryLoansToCycle: {}", businessDate);
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(mqQueueNames.getBusinessDateReplyQueue()).build();
        businessDateSendChannel.basicPublish(mqQueueNames.getExchange(), mqQueueNames.getAccountQueryLoansToCycleQueue(), props, SerializationUtils.serialize(businessDate));
        return replyWaitingHandler.getReply(responseKey);
    }

    public void serviceRequestBillLoan(BillingCycle billingCycle) throws IOException {
        businessDateSendChannel.basicPublish(mqQueueNames.getExchange(), mqQueueNames.getServiceRequestBillLoanQueue(), null, SerializationUtils.serialize(billingCycle));
    }

}
