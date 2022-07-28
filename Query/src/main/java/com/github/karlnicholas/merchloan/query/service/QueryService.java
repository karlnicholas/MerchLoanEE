package com.github.karlnicholas.merchloan.query.service;

import com.github.karlnicholas.merchloan.replywaiting.ReplyWaitingHandler;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.*;
import javax.ws.rs.core.Response;
import java.util.UUID;

@ApplicationScoped
@Slf4j
public class QueryService {
    private final JMSContext jmsContext;
    @Resource(lookup = "java:global/jms/queue/ServiceRequestQueryIdQueue")
    private Queue serviceRequestQueryIdQueue;
    private final TemporaryQueue queryIdReplyQueue;
    private final ReplyWaitingHandler replyWaitingHandlerQueryId;
    @Resource(lookup = "java:global/jms/queue/ServiceRequestCheckRequestQueue")
    private Queue serviceRequestCheckRequestQueue;
    private final TemporaryQueue checkRequestReplyQueue;
    private final ReplyWaitingHandler replyWaitingHandlerCheckRequest;
    @Resource(lookup = "java:global/jms/queue/AccountsAccountIdQueue")
    private Queue accountsAccountIdQueue;
    private final TemporaryQueue accountsAccountIdReplyQueue;
    private final ReplyWaitingHandler replyWaitingHandlerAccountId;
    @Resource(lookup = "java:global/jms/queue/AccountsLoanIdQueue")
    private Queue accountsLoanIdQueue;
    private final TemporaryQueue accountsLoanIdReplyQueue;
    private final ReplyWaitingHandler replyWaitingHandlerLoanId;
    @Resource(lookup = "java:global/jms/queue/StatementQueryStatementQueue")
    private Queue statementQueryStatementQueue;
    private final TemporaryQueue statementQueryStatementReplyQueue;
    private final ReplyWaitingHandler replyWaitingHandlerQueryStatement;
    @Resource(lookup = "java:global/jms/queue/StatementQueryStatementsQueue")
    private Queue statementQueryStatementsQueue;
    private final TemporaryQueue statementQueryStatementsReplyQueue;
    private final ReplyWaitingHandler replyWaitingHandlerQueryStatements;

    @Inject
    public QueryService(JMSContext jmsContext) throws JMSException {
        this.jmsContext = jmsContext;

        replyWaitingHandlerQueryId = new ReplyWaitingHandler();
        queryIdReplyQueue = jmsContext.createTemporaryQueue();
        JMSConsumer queryIdReplyConsumer = jmsContext.createConsumer(queryIdReplyQueue);
        queryIdReplyConsumer.setMessageListener(m-> {
            try {
                replyWaitingHandlerQueryId.handleReply(m.getJMSCorrelationID(), m.getBody(Object.class));
            } catch (JMSException e) {
                log.error("replyWaitingHandlerQueryId ", e);
            }
        });

        replyWaitingHandlerCheckRequest = new ReplyWaitingHandler();
        checkRequestReplyQueue = jmsContext.createTemporaryQueue();
        JMSConsumer checkRequestReplyConsumer = jmsContext.createConsumer(checkRequestReplyQueue);
        checkRequestReplyConsumer.setMessageListener(m-> {
            try {
                replyWaitingHandlerCheckRequest.handleReply(m.getJMSCorrelationID(), m.getBody(Object.class));
            } catch (JMSException e) {
                log.error("replyWaitingHandlerCheckRequest ", e);
            }
        });

        replyWaitingHandlerAccountId = new ReplyWaitingHandler();
        accountsAccountIdReplyQueue = jmsContext.createTemporaryQueue();
        JMSConsumer accountsAccountIdReplyConsumer = jmsContext.createConsumer(accountsAccountIdReplyQueue);
        accountsAccountIdReplyConsumer.setMessageListener(m-> {
            try {
                replyWaitingHandlerAccountId.handleReply(m.getJMSCorrelationID(), m.getBody(Object.class));
            } catch (JMSException e) {
                log.error("replyWaitingHandlerAccountId ", e);
            }
        });

        replyWaitingHandlerLoanId = new ReplyWaitingHandler();
        accountsLoanIdReplyQueue = jmsContext.createTemporaryQueue();
        JMSConsumer accountsLoanIdReplyConsumer = jmsContext.createConsumer(accountsLoanIdReplyQueue);
        accountsLoanIdReplyConsumer.setMessageListener(m-> {
            try {
                replyWaitingHandlerLoanId.handleReply(m.getJMSCorrelationID(), m.getBody(Object.class));
            } catch (JMSException e) {
                log.error("replyWaitingHandlerLoanId ", e);
            }
        });

        replyWaitingHandlerQueryStatement = new ReplyWaitingHandler();
        statementQueryStatementReplyQueue = jmsContext.createTemporaryQueue();
        JMSConsumer statementQueryStatementReplyConsumer = jmsContext.createConsumer(statementQueryStatementReplyQueue);
        statementQueryStatementReplyConsumer.setMessageListener(m-> {
            try {
                replyWaitingHandlerQueryStatement.handleReply(m.getJMSCorrelationID(), m.getBody(Object.class));
            } catch (JMSException e) {
                log.error("replyWaitingHandlerQueryStatement ", e);
            }
        });

        replyWaitingHandlerQueryStatements = new ReplyWaitingHandler();
        statementQueryStatementsReplyQueue = jmsContext.createTemporaryQueue();
        JMSConsumer statementQueryStatementsReplyConsumer = jmsContext.createConsumer(statementQueryStatementsReplyQueue);
        statementQueryStatementsReplyConsumer.setMessageListener(m-> {
            try {
                replyWaitingHandlerQueryStatements.handleReply(m.getJMSCorrelationID(), m.getBody(Object.class));
            } catch (JMSException e) {
                log.error("replyWaitingHandlerQueryStatements ", e);
            }
        });
    }

    public Response getRequest(UUID id) throws JMSException, InterruptedException {
        JMSProducer producer = jmsContext.createProducer();
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        ObjectMessage message = jmsContext.createObjectMessage(id);
        String correlationId = UUID.randomUUID().toString();
        message.setJMSReplyTo(queryIdReplyQueue);
        message.setJMSCorrelationID(correlationId);
        replyWaitingHandlerQueryId.put(correlationId);
        producer.send(serviceRequestQueryIdQueue, message);
        return Response.ok(replyWaitingHandlerQueryId.getReply(correlationId)).build();
    }

    public Response getCheckRequests() throws JMSException, InterruptedException {
        JMSProducer producer = jmsContext.createProducer();
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        ObjectMessage message = jmsContext.createObjectMessage(new byte[0]);
        String correlationId = UUID.randomUUID().toString();
        message.setJMSReplyTo(checkRequestReplyQueue);
        message.setJMSCorrelationID(correlationId);
        replyWaitingHandlerCheckRequest.put(correlationId);
        producer.send(serviceRequestCheckRequestQueue, message);
        return Response.ok(replyWaitingHandlerCheckRequest.getReply(correlationId)).build();
    }

    public Response getAccount(UUID id) throws JMSException, InterruptedException {
        JMSProducer producer = jmsContext.createProducer();
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        ObjectMessage message = jmsContext.createObjectMessage(id);
        String correlationId = UUID.randomUUID().toString();
        message.setJMSReplyTo(accountsAccountIdReplyQueue);
        message.setJMSCorrelationID(correlationId);
        replyWaitingHandlerAccountId.put(correlationId);
        producer.send(accountsAccountIdQueue, message);
        return Response.ok(replyWaitingHandlerAccountId.getReply(correlationId)).build();
    }

    public Response getLoan(UUID id) throws JMSException, InterruptedException {
        JMSProducer producer = jmsContext.createProducer();
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        ObjectMessage message = jmsContext.createObjectMessage(id);
        String correlationId = UUID.randomUUID().toString();
        message.setJMSReplyTo(accountsLoanIdReplyQueue);
        message.setJMSCorrelationID(correlationId);
        replyWaitingHandlerLoanId.put(correlationId);
        producer.send(accountsLoanIdQueue, message);
        return Response.ok(replyWaitingHandlerLoanId.getReply(correlationId)).build();
    }

    public Response getStatement(UUID id) throws JMSException, InterruptedException {
        JMSProducer producer = jmsContext.createProducer();
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        ObjectMessage message = jmsContext.createObjectMessage(id);
        String correlationId = UUID.randomUUID().toString();
        message.setJMSReplyTo(statementQueryStatementReplyQueue);
        message.setJMSCorrelationID(correlationId);
        replyWaitingHandlerQueryStatement.put(correlationId);
        producer.send(statementQueryStatementQueue, message);
        return Response.ok(replyWaitingHandlerQueryStatement.getReply(correlationId)).build();
    }

    public Response getStatements(UUID id) throws JMSException, InterruptedException {
        JMSProducer producer = jmsContext.createProducer();
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        ObjectMessage message = jmsContext.createObjectMessage(id);
        String correlationId = UUID.randomUUID().toString();
        message.setJMSReplyTo(statementQueryStatementsReplyQueue);
        message.setJMSCorrelationID(correlationId);
        replyWaitingHandlerQueryStatements.put(correlationId);
        producer.send(statementQueryStatementsQueue, message);
        return Response.ok(replyWaitingHandlerQueryStatements.getReply(correlationId)).build();
    }
}
