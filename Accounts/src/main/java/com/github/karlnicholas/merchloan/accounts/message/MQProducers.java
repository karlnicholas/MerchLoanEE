package com.github.karlnicholas.merchloan.accounts.message;

import com.github.karlnicholas.merchloan.jms.ReplyWaitingHandler;
import com.github.karlnicholas.merchloan.jms.MQConsumerUtils;
import com.github.karlnicholas.merchloan.jmsmessage.ServiceRequestResponse;
import com.github.karlnicholas.merchloan.jmsmessage.StatementHeader;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
public class MQProducers {
    private final MQConsumerUtils mqConsumerUtils;
    private final Channel accountSendChannel;
    private final ReplyWaitingHandler replyWaitingHandler;
    private final String accountsReplyQueue;

    @Autowired
    public MQProducers(Connection connection, MQConsumerUtils mqConsumerUtils) throws IOException {
        this.mqConsumerUtils = mqConsumerUtils;
        replyWaitingHandler = new ReplyWaitingHandler();
        accountsReplyQueue = "accounts-reply-"+UUID.randomUUID();
        accountSendChannel = connection.createChannel();

        mqConsumerUtils.bindConsumer(connection, mqConsumerUtils.getExchange(), accountsReplyQueue, true, replyWaitingHandler::handleReplies);
    }

    public void serviceRequestServiceRequest(ServiceRequestResponse serviceRequest) throws IOException {
        log.debug("serviceRequestServiceRequest: {}", serviceRequest);
        accountSendChannel.basicPublish(mqConsumerUtils.getExchange(), mqConsumerUtils.getServicerequestQueue(), null, SerializationUtils.serialize(serviceRequest));
    }

    public void statementCloseStatement(StatementHeader statementHeader) throws IOException {
        log.debug("statementCloseStatement: {}", statementHeader);
        accountSendChannel.basicPublish(mqConsumerUtils.getExchange(), mqConsumerUtils.getStatementCloseStatementQueue(), null, SerializationUtils.serialize(statementHeader));
    }

    public Object queryMostRecentStatement(UUID loanId) throws IOException, InterruptedException {
        log.debug("queryMostRecentStatement: {}", loanId);
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(accountsReplyQueue).build();
        accountSendChannel.basicPublish(mqConsumerUtils.getExchange(), mqConsumerUtils.getStatementQueryMostRecentStatementQueue(), props, SerializationUtils.serialize(loanId));
        return replyWaitingHandler.getReply(responseKey);
    }

}
