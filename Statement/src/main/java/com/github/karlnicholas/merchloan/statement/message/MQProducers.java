package com.github.karlnicholas.merchloan.statement.message;

import com.github.karlnicholas.merchloan.jms.MQConsumerUtils;
import com.github.karlnicholas.merchloan.jms.ReplyWaitingHandler;
import com.github.karlnicholas.merchloan.jmsmessage.BillingCycleCharge;
import com.github.karlnicholas.merchloan.jmsmessage.ServiceRequestResponse;
import com.github.karlnicholas.merchloan.jmsmessage.StatementCompleteResponse;
import com.github.karlnicholas.merchloan.jmsmessage.StatementHeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.*;
import java.util.UUID;

@Component
@Slf4j
public class MQProducers {
    private final Session session;
    private final MessageProducer statementProducer;
    private final ReplyWaitingHandler replyWaitingHandler;
    private final Destination statementReplyQueue;
    private final Destination accountBillingCycleChargeQueue;
    private final Destination accountQueryStatementHeaderQueue;
    private final Destination servicerequestQueue;
    private final Destination accountLoanClosedQueue;
    private final Destination serviceRequestStatementCompleteQueue;

    @Autowired
    public MQProducers(Session session, MQConsumerUtils mqConsumerUtils) throws JMSException {
        this.session = session;
        statementReplyQueue = session.createTemporaryQueue();
        replyWaitingHandler = new ReplyWaitingHandler();
        statementProducer = session.createProducer(null);
        accountBillingCycleChargeQueue = session.createQueue(mqConsumerUtils.getAccountBillingCycleChargeQueue());
        accountQueryStatementHeaderQueue = session.createQueue(mqConsumerUtils.getAccountQueryStatementHeaderQueue());
        servicerequestQueue = session.createQueue(mqConsumerUtils.getServicerequestQueue());
        accountLoanClosedQueue = session.createQueue(mqConsumerUtils.getAccountLoanClosedQueue());
        serviceRequestStatementCompleteQueue = session.createQueue(mqConsumerUtils.getServiceRequestStatementCompleteQueue());
        mqConsumerUtils.bindConsumer(session, statementReplyQueue, replyWaitingHandler::onMessage);
    }

    public Object accountBillingCycleCharge(BillingCycleCharge billingCycleCharge) throws InterruptedException, JMSException {
        log.debug("accountBillingCycleCharge: {}", billingCycleCharge);
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        Message message = session.createObjectMessage(billingCycleCharge);
        message.setJMSCorrelationID(responseKey);
        message.setJMSReplyTo(statementReplyQueue);
        statementProducer.send(accountBillingCycleChargeQueue, message);
        return replyWaitingHandler.getReply(responseKey);
    }

    public Object accountQueryStatementHeader(StatementHeader statementHeader) throws InterruptedException, JMSException {
        log.debug("accountQueryStatementHeader: {}", statementHeader);
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        Message message = session.createObjectMessage(statementHeader);
        message.setJMSCorrelationID(responseKey);
        message.setJMSReplyTo(statementReplyQueue);
        statementProducer.send(accountQueryStatementHeaderQueue, message);
        return replyWaitingHandler.getReply(responseKey);
    }

    public void serviceRequestServiceRequest(ServiceRequestResponse serviceRequest) throws JMSException {
        log.debug("serviceRequestServiceRequest: {}", serviceRequest);
        statementProducer.send(servicerequestQueue, session.createObjectMessage(serviceRequest));
    }

    public void accountLoanClosed(StatementHeader statementHeader) throws JMSException {
        log.debug("accountLoanClosed: {}", statementHeader);
        statementProducer.send(accountLoanClosedQueue, session.createObjectMessage(statementHeader));
    }

    public void serviceRequestStatementComplete(StatementCompleteResponse requestResponse) throws JMSException {
        log.debug("serviceRequestStatementComplete: {}", requestResponse);
        statementProducer.send(serviceRequestStatementCompleteQueue, session.createObjectMessage(requestResponse));
    }

}
