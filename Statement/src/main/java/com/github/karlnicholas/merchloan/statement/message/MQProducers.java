package com.github.karlnicholas.merchloan.statement.message;

import com.github.karlnicholas.merchloan.jms.MQConsumerUtils;
import com.github.karlnicholas.merchloan.jms.ReplyWaitingHandler;
import com.github.karlnicholas.merchloan.jmsmessage.BillingCycleCharge;
import com.github.karlnicholas.merchloan.jmsmessage.ServiceRequestResponse;
import com.github.karlnicholas.merchloan.jmsmessage.StatementCompleteResponse;
import com.github.karlnicholas.merchloan.jmsmessage.StatementHeader;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.io.IOException;
import java.util.UUID;

@Component
@Slf4j
public class MQProducers {
    private final MQConsumerUtils mqConsumerUtils;
    private final Channel statementSendChannel;
    private final ReplyWaitingHandler replyWaitingHandler;
    private final String statementReplyQueue;

    @Autowired
    public MQProducers(Connection connection, MQConsumerUtils mqConsumerUtils) throws IOException {
        this.mqConsumerUtils = mqConsumerUtils;
        statementReplyQueue = "statement-reply-"+UUID.randomUUID();
        replyWaitingHandler = new ReplyWaitingHandler();
        statementSendChannel = connection.createChannel();
        mqConsumerUtils.bindConsumer(connection, mqConsumerUtils.getExchange(), statementReplyQueue, true, replyWaitingHandler::handleReplies);
    }

    public Object accountBillingCycleCharge(BillingCycleCharge billingCycleCharge) throws IOException, InterruptedException {
        log.debug("accountBillingCycleCharge: {}", billingCycleCharge);
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(statementReplyQueue).build();
        statementSendChannel.basicPublish(mqConsumerUtils.getExchange(), mqConsumerUtils.getAccountBillingCycleChargeQueue(), props, SerializationUtils.serialize(billingCycleCharge));
        return replyWaitingHandler.getReply(responseKey);
    }

    public Object accountQueryStatementHeader(StatementHeader statementHeader) throws IOException, InterruptedException {
        log.debug("accountQueryStatementHeader: {}", statementHeader);
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(statementReplyQueue).build();
        statementSendChannel.basicPublish(mqConsumerUtils.getExchange(), mqConsumerUtils.getAccountQueryStatementHeaderQueue(), props, SerializationUtils.serialize(statementHeader));
        return replyWaitingHandler.getReply(responseKey);
    }

    public void serviceRequestServiceRequest(ServiceRequestResponse serviceRequest) throws IOException {
        log.debug("serviceRequestServiceRequest: {}", serviceRequest);
        statementSendChannel.basicPublish(mqConsumerUtils.getExchange(), mqConsumerUtils.getServicerequestQueue(), null, SerializationUtils.serialize(serviceRequest));
    }

    public void accountLoanClosed(StatementHeader statementHeader) throws IOException {
        log.debug("accountLoanClosed: {}", statementHeader);
        statementSendChannel.basicPublish(mqConsumerUtils.getExchange(), mqConsumerUtils.getAccountLoanClosedQueue(), null, SerializationUtils.serialize(statementHeader));
    }

    public void serviceRequestStatementComplete(StatementCompleteResponse requestResponse) throws IOException {
        log.debug("serviceRequestStatementComplete: {}", requestResponse);
        statementSendChannel.basicPublish(mqConsumerUtils.getExchange(), mqConsumerUtils.getServiceRequestStatementCompleteQueue(), null, SerializationUtils.serialize(requestResponse));
    }

}
