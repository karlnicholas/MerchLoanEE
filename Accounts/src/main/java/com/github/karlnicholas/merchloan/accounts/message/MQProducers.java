package com.github.karlnicholas.merchloan.accounts.message;

import com.github.karlnicholas.merchloan.jms.MQConsumerUtils;
import com.github.karlnicholas.merchloan.jms.ReplyWaitingHandler;
import com.github.karlnicholas.merchloan.jmsmessage.ServiceRequestResponse;
import com.github.karlnicholas.merchloan.jmsmessage.StatementHeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.jms.*;

import java.util.UUID;

@Service
@Slf4j
public class MQProducers {
    private final ReplyWaitingHandler replyWaitingHandler;
    private final JMSContext accountsReplyContext;
    private final Destination accountsReplyQueue;
    private final JMSContext servicerequestContext;
    private final Destination servicerequestQueue;
    private final JMSContext statementCloseStatementContext;
    private final Destination statementCloseStatementQueue;
    private final JMSContext statementQueryMostRecentStatementContext;
    private final Destination statementQueryMostRecentStatementQueue;

    @Autowired
    public MQProducers(ConnectionFactory connectionFactory, MQConsumerUtils mqConsumerUtils) throws JMSException {
        servicerequestContext = connectionFactory.createContext();
        servicerequestQueue = servicerequestContext.createQueue(mqConsumerUtils.getServicerequestQueue());

        statementCloseStatementContext = connectionFactory.createContext();
        statementCloseStatementQueue = statementCloseStatementContext.createQueue(mqConsumerUtils.getStatementCloseStatementQueue());

        statementQueryMostRecentStatementContext = connectionFactory.createContext();
        statementQueryMostRecentStatementQueue = statementQueryMostRecentStatementContext.createQueue(mqConsumerUtils.getStatementQueryMostRecentStatementQueue());


        accountsReplyContext = connectionFactory.createContext();
        replyWaitingHandler = new ReplyWaitingHandler();
        accountsReplyQueue = accountsReplyContext.createTemporaryQueue();
        accountsReplyContext.createConsumer(accountsReplyQueue).setMessageListener(replyWaitingHandler::onMessage);

    }

    public void serviceRequestServiceRequest(ServiceRequestResponse serviceRequest) throws JMSException {
        log.debug("serviceRequestServiceRequest: {}", serviceRequest);
        servicerequestContext.createProducer().send(servicerequestQueue, servicerequestContext.createObjectMessage(serviceRequest));
    }

    public void statementCloseStatement(StatementHeader statementHeader) {
        log.debug("statementCloseStatement: {}", statementHeader);
        statementCloseStatementContext.createProducer().send(statementCloseStatementQueue, statementCloseStatementContext.createObjectMessage(statementHeader));
    }

    public Object queryMostRecentStatement(UUID loanId) throws InterruptedException, JMSException {
        log.debug("queryMostRecentStatement: {}", loanId);
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        Message message = accountsReplyContext.createObjectMessage(loanId);
        message.setJMSCorrelationID(responseKey);
        message.setJMSReplyTo(accountsReplyQueue);
        accountsReplyContext.createProducer().send(statementQueryMostRecentStatementQueue, message);
        return replyWaitingHandler.getReply(responseKey);
    }
}
