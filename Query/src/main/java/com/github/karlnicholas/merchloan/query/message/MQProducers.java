package com.github.karlnicholas.merchloan.query.message;

import com.github.karlnicholas.merchloan.servicerequestinterface.message.ServiceRequestBeans;
import lombok.extern.slf4j.Slf4j;

import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
@Slf4j
public class MQProducers {
//    @Resource(lookup = "java:jboss/exported/jms/RemoteConnectionFactory")
//    private ConnectionFactory connectionFactory;
//    @Resource(lookup = "java:global/jms/queue/ServiceRequestQueryIdQueue")
//    private Queue serviceRequestQueryIdQueue;
//    @Resource(lookup = "java:global/jms/queue/AccountQueryAccountIdQueue")
//    private Queue accountQueryAccountIdQueue;
//    @Resource(lookup = "java:global/jms/queue/AccountQueryLoanIdQueue")
//    private Queue accountQueryLoanIdQueue;
//    @Resource(lookup = "java:global/jms/queue/StatementQueryStatementQueue")
//    private Queue statementQueryStatementQueue;
//    @Resource(lookup = "java:global/jms/queue/StatementQueryStatementsQueue")
//    private Queue statementQueryStatementsQueue;
    @EJB(lookup = "ejb:/servicerequest-1.0-SNAPSHOT/ServiceRequestBeansImpl!com.github.karlnicholas.merchloan.servicerequestinterface.message.ServiceRequestBeans")
    private ServiceRequestBeans serviceRequestBeans;

    public Object queryServiceRequest(UUID id) throws Throwable {
        return serviceRequestBeans.queryId(id);
    }

    public Object queryCheckRequest() throws Throwable {
        return serviceRequestBeans.checkRequest();
    }

    //    public Object queryAccount(UUID id) {
//        Instant start = Instant.now();
//        String responseKey = UUID.randomUUID().toString();
//        replyWaitingHandler.put(responseKey);
//        try (JMSContext jmsContext = connectionFactory.createContext()) {
//            Message message = jmsContext.createObjectMessage(id);
//            message.setJMSCorrelationID(responseKey);
//            message.setJMSReplyTo(queryReplyQueue);
//            jmsContext.createProducer().send(accountQueryAccountIdQueue, message);
//            Object r = replyWaitingHandler.getReply(responseKey);
//            log.debug("queryAccount {}", Duration.between(Instant.now(), start));
//            return r;
//        } catch (JMSException | InterruptedException e) {
//            log.error("queryAccount", e);
//            Thread.currentThread().interrupt();
//            return null;
//        }
//    }
//
//    public Object queryLoan(UUID id) {
//        Instant start = Instant.now();
//        String responseKey = UUID.randomUUID().toString();
//        replyWaitingHandler.put(responseKey);
//        try (JMSContext jmsContext = connectionFactory.createContext()) {
//            Message message = jmsContext.createObjectMessage(id);
//            message.setJMSCorrelationID(responseKey);
//            message.setJMSReplyTo(queryReplyQueue);
//            jmsContext.createProducer().send(accountQueryLoanIdQueue, message);
//            Object r = replyWaitingHandler.getReply(responseKey);
//            log.debug("queryLoan {}", Duration.between(Instant.now(), start));
//            return r;
//        } catch (JMSException | InterruptedException e) {
//            log.error("queryLoan", e);
//            Thread.currentThread().interrupt();
//            return null;
//        }
//    }
//
//    public Object queryStatement(UUID id) {
//        Instant start = Instant.now();
//        String responseKey = UUID.randomUUID().toString();
//        replyWaitingHandler.put(responseKey);
//        try (JMSContext jmsContext = connectionFactory.createContext()) {
//            Message message = jmsContext.createObjectMessage(id);
//            message.setJMSCorrelationID(responseKey);
//            message.setJMSReplyTo(queryReplyQueue);
//            jmsContext.createProducer().send(statementQueryStatementQueue, message);
//            Object r = replyWaitingHandler.getReply(responseKey);
//            log.debug("queryStatement {}", Duration.between(Instant.now(), start));
//            return r;
//        } catch (JMSException | InterruptedException e) {
//            log.error("queryStatement", e);
//            Thread.currentThread().interrupt();
//            return null;
//        }
//    }
//
//    public Object queryStatements(UUID id) {
//        Instant start = Instant.now();
//        String responseKey = UUID.randomUUID().toString();
//        replyWaitingHandler.put(responseKey);
//        try (JMSContext jmsContext = connectionFactory.createContext()) {
//            Message message = jmsContext.createObjectMessage(id);
//            message.setJMSCorrelationID(responseKey);
//            message.setJMSReplyTo(queryReplyQueue);
//            jmsContext.createProducer().send(statementQueryStatementsQueue, message);
//            Object r = replyWaitingHandler.getReply(responseKey);
//            log.debug("queryStatements {}", Duration.between(Instant.now(), start));
//            return r;
//        } catch (JMSException | InterruptedException e) {
//            log.error("queryStatements", e);
//            Thread.currentThread().interrupt();
//            return null;
//        }
//    }

}
