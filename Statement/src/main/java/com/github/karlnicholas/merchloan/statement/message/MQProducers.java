package com.github.karlnicholas.merchloan.statement.message;

import com.github.karlnicholas.merchloan.jms.ReplyWaitingHandler;
import com.github.karlnicholas.merchloan.jms.config.MQQueueNames;
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
    private final MQQueueNames mqQueueNames;
    private final Channel statementSendChannel;
    private final ReplyWaitingHandler replyWaitingHandler;

    @Autowired
    public MQProducers(Connection connection, MQQueueNames mqQueueNames) throws IOException {
        this.mqQueueNames = mqQueueNames;
        replyWaitingHandler = new ReplyWaitingHandler();
        statementSendChannel = connection.createChannel();

        mqQueueNames.bindConsumer(connection, mqQueueNames.getExchange(), mqQueueNames.getAccountReplyQueue(), replyWaitingHandler::handleReplies);
    }

    public Object accountBillingCycleCharge(BillingCycleCharge billingCycleCharge) throws IOException, InterruptedException {
        log.debug("accountBillingCycleCharge: {}", billingCycleCharge);
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(mqQueueNames.getStatementReplyQueue()).build();
        statementSendChannel.basicPublish(mqQueueNames.getExchange(), mqQueueNames.getAccountBillingCycleChargeQueue(), props, SerializationUtils.serialize(billingCycleCharge));
        return replyWaitingHandler.getReply(responseKey);
    }

    public Object accountQueryStatementHeader(StatementHeader statementHeader) throws IOException, InterruptedException {
        log.debug("accountQueryStatementHeader: {}", statementHeader);
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(mqQueueNames.getStatementReplyQueue()).build();
        statementSendChannel.basicPublish(mqQueueNames.getExchange(), mqQueueNames.getAccountQueryStatementHeaderQueue(), props, SerializationUtils.serialize(statementHeader));
        return replyWaitingHandler.getReply(responseKey);
    }

    public void serviceRequestServiceRequest(ServiceRequestResponse serviceRequest) throws IOException {
        log.debug("serviceRequestServiceRequest: {}", serviceRequest);
        statementSendChannel.basicPublish(mqQueueNames.getExchange(), mqQueueNames.getServicerequestQueue(), null, SerializationUtils.serialize(serviceRequest));
    }

    public void accountLoanClosed(StatementHeader statementHeader) throws IOException {
        log.debug("accountLoanClosed: {}", statementHeader);
        statementSendChannel.basicPublish(mqQueueNames.getExchange(), mqQueueNames.getAccountLoanClosedQueue(), null, SerializationUtils.serialize(statementHeader));
    }

    public void serviceRequestStatementComplete(StatementCompleteResponse requestResponse) throws IOException {
        log.debug("serviceRequestStatementComplete: {}", requestResponse);
        statementSendChannel.basicPublish(mqQueueNames.getExchange(), mqQueueNames.getServiceRequestStatementCompleteQueue(), null, SerializationUtils.serialize(requestResponse));
    }

}
