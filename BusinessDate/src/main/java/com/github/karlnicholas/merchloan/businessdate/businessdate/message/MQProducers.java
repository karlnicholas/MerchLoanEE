package com.github.karlnicholas.merchloan.businessdate.businessdate.message;

import com.github.karlnicholas.merchloan.jms.ReplyWaitingHandler;
import com.github.karlnicholas.merchloan.jms.MQConsumerUtils;
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
    private final MQConsumerUtils mqConsumerUtils;
    private final Channel businessDateSendChannel;
    private final ReplyWaitingHandler replyWaitingHandler;
    @Autowired
    public MQProducers(Connection connection, MQConsumerUtils mqConsumerUtils) throws IOException {
        this.mqConsumerUtils = mqConsumerUtils;
        replyWaitingHandler = new ReplyWaitingHandler();

        businessDateSendChannel = connection.createChannel();
        mqConsumerUtils.bindConsumer(connection, mqConsumerUtils.getExchange(), mqConsumerUtils.getBusinessDateReplyQueue(), replyWaitingHandler::handleReplies);
    }

    public Object servicerequestCheckRequest() throws IOException, InterruptedException {
        log.debug("servicerequestCheckRequest:");
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(mqConsumerUtils.getBusinessDateReplyQueue()).build();
        businessDateSendChannel.basicPublish(mqConsumerUtils.getExchange(), mqConsumerUtils.getServiceRequestCheckRequestQueue(), props, SerializationUtils.serialize(new byte[0]));
        return replyWaitingHandler.getReply(responseKey);
    }

    public Object acccountQueryLoansToCycle(LocalDate businessDate) throws IOException, InterruptedException {
        log.debug("acccountQueryLoansToCycle: {}", businessDate);
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(mqConsumerUtils.getBusinessDateReplyQueue()).build();
        businessDateSendChannel.basicPublish(mqConsumerUtils.getExchange(), mqConsumerUtils.getAccountQueryLoansToCycleQueue(), props, SerializationUtils.serialize(businessDate));
        return replyWaitingHandler.getReply(responseKey);
    }

    public void serviceRequestBillLoan(BillingCycle billingCycle) throws IOException {
        businessDateSendChannel.basicPublish(mqConsumerUtils.getExchange(), mqConsumerUtils.getServiceRequestBillLoanQueue(), null, SerializationUtils.serialize(billingCycle));
    }

}
