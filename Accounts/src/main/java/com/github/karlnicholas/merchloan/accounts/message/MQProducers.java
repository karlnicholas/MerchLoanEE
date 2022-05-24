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
    private final Queue accountsReplyQueue;
    private final Destination servicerequestQueue;
    private final Destination statementCloseStatementQueue;
    private final Destination statementQueryMostRecentStatementQueue;

    @Autowired
    public MQProducers(Connection connection, MQConsumerUtils mqConsumerUtils) throws JMSException {
        try (Session session = connection.createSession()) {
            replyWaitingHandler = new ReplyWaitingHandler();
            servicerequestQueue = session.createQueue(mqConsumerUtils.getServicerequestQueue());
            statementCloseStatementQueue = session.createQueue(mqConsumerUtils.getStatementCloseStatementQueue());
            statementQueryMostRecentStatementQueue = session.createQueue(mqConsumerUtils.getStatementQueryMostRecentStatementQueue());
        }
        Session consumerSession = connection.createSession();
        accountsReplyQueue = consumerSession.createTemporaryQueue();
        mqConsumerUtils.bindConsumer(consumerSession, accountsReplyQueue, replyWaitingHandler::onMessage);
        connection.start();
    }

    public void serviceRequestServiceRequest(Session session, ServiceRequestResponse serviceRequest) throws JMSException {
        log.debug("serviceRequestServiceRequest: {}", serviceRequest);
            try(MessageProducer producer = session.createProducer(servicerequestQueue)) {
                producer.send(session.createObjectMessage(serviceRequest));
            }
    }

    public void statementCloseStatement(Session session, StatementHeader statementHeader) throws JMSException {
        log.debug("statementCloseStatement: {}", statementHeader);
        try(MessageProducer producer = session.createProducer(statementCloseStatementQueue)) {
            producer.send(session.createObjectMessage(statementHeader));
        }
    }

    public Object queryMostRecentStatement(Session session, UUID loanId) throws InterruptedException, JMSException {
        log.debug("queryMostRecentStatement: {}", loanId);
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        Message message = session.createObjectMessage(loanId);
        message.setJMSCorrelationID(responseKey);
        message.setJMSReplyTo(accountsReplyQueue);
        try ( MessageProducer producer = session.createProducer(statementQueryMostRecentStatementQueue)) {
            producer.send(message);
            return replyWaitingHandler.getReply(responseKey);
        }
    }

}
