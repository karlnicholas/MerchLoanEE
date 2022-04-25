package com.github.karlnicholas.merchloan.accounts.message;

import com.github.karlnicholas.merchloan.jms.ReplyWaitingHandler;
import com.github.karlnicholas.merchloan.jms.config.MQQueueNames;
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
    private final MQQueueNames mqQueueNames;
    private final Channel accountSendChannel;
    private final ReplyWaitingHandler replyWaitingHandler;

    @Autowired
    public MQProducers(Connection connection, MQQueueNames mqQueueNames) throws IOException {
        this.mqQueueNames = mqQueueNames;
        replyWaitingHandler = new ReplyWaitingHandler();

        accountSendChannel = connection.createChannel();

        mqQueueNames.bindConsumer(connection, mqQueueNames.getExchange(), mqQueueNames.getAccountReplyQueue(), replyWaitingHandler::handleReplies);
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
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(mqQueueNames.getAccountReplyQueue()).build();
        accountSendChannel.basicPublish(mqQueueNames.getExchange(), mqQueueNames.getStatementQueryMostRecentStatementQueue(), props, SerializationUtils.serialize(loanId));
        return replyWaitingHandler.getReply(responseKey);
    }

}
