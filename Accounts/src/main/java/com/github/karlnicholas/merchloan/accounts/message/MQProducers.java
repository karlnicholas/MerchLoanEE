package com.github.karlnicholas.merchloan.accounts.message;

import com.github.karlnicholas.merchloan.jms.MQConsumerUtils;
import com.github.karlnicholas.merchloan.jms.ReplyWaitingHandler;
import com.github.karlnicholas.merchloan.jmsmessage.ServiceRequestResponse;
import com.github.karlnicholas.merchloan.jmsmessage.StatementHeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jms.*;
import java.util.UUID;

@Service
@Slf4j
public class MQProducers {
    private final Session session;
    private final MessageProducer accountProducer;
    private final ReplyWaitingHandler replyWaitingHandler;
    private final Queue accountsReplyQueue;
    private final Destination servicerequestQueue;
    private final Destination statementCloseStatementQueue;
    private final Destination statementQueryMostRecentStatementQueue;

    @Autowired
    public MQProducers(Connection connection, MQConsumerUtils mqConsumerUtils) throws JMSException {
        this.session = connection.createSession();
        replyWaitingHandler = new ReplyWaitingHandler();
        accountsReplyQueue = session.createTemporaryQueue();
        accountProducer = session.createProducer(null);
        servicerequestQueue = session.createQueue(mqConsumerUtils.getServicerequestQueue());
        statementCloseStatementQueue = session.createQueue(mqConsumerUtils.getStatementCloseStatementQueue());
        statementQueryMostRecentStatementQueue = session.createQueue(mqConsumerUtils.getStatementQueryMostRecentStatementQueue());
        mqConsumerUtils.bindConsumer(session, accountsReplyQueue, replyWaitingHandler::onMessage);
        connection.start();
    }

    public void serviceRequestServiceRequest(ServiceRequestResponse serviceRequest) throws JMSException {
        log.debug("serviceRequestServiceRequest: {}", serviceRequest);
        accountProducer.send(servicerequestQueue, session.createObjectMessage(serviceRequest));
    }

    public void statementCloseStatement(StatementHeader statementHeader) throws JMSException {
        log.debug("statementCloseStatement: {}", statementHeader);
        accountProducer.send(statementCloseStatementQueue, session.createObjectMessage(statementHeader));
    }

    public Object queryMostRecentStatement(UUID loanId) throws InterruptedException, JMSException {
        log.debug("queryMostRecentStatement: {}", loanId);
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        Message message = session.createObjectMessage(loanId);
        message.setJMSCorrelationID(responseKey);
        message.setJMSReplyTo(accountsReplyQueue);
        accountProducer.send(statementQueryMostRecentStatementQueue, message);
        return replyWaitingHandler.getReply(responseKey);
    }

}
