package com.github.karlnicholas.merchloan.accounts.message;

import com.github.karlnicholas.merchloan.jmsmessage.ServiceRequestResponse;
import com.github.karlnicholas.merchloan.jmsmessage.StatementHeader;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSException;

@ApplicationScoped
@Slf4j
public class MQProducers {
    @Resource(lookup = "java:jboss/exported/jms/RemoteConnectionFactory")
    private ConnectionFactory connectionFactory;
    @Resource(lookup = "java:global/jms/queue/ServiceRequestResponseQueue")
    private Destination serviceRequestQueue;
    @Resource(lookup = "java:global/jms/queue/StatementCloseStatementQueue")
    private Destination statementCloseStatementQueue;
    @Resource(lookup = "java:global/jms/queue/StatementQueryMostRecentStatementQueue")
    private Destination statementQueryMostRecentStatementQueue;

    public void serviceRequestServiceRequest(ServiceRequestResponse serviceRequest) throws JMSException {
        log.debug("serviceRequestServiceRequest: {}", serviceRequest);
        try ( JMSContext jmsContext = connectionFactory.createContext()) {
            jmsContext.createProducer().send(serviceRequestQueue, jmsContext.createObjectMessage(serviceRequest));
        }
    }

    public void statementCloseStatement(StatementHeader statementHeader) {
        log.debug("statementCloseStatement: {}", statementHeader);
        try ( JMSContext jmsContext = connectionFactory.createContext()) {
            jmsContext.createProducer().send(statementCloseStatementQueue, jmsContext.createObjectMessage(statementHeader));
        }
    }

//    public Object queryMostRecentStatement(UUID loanId) throws InterruptedException, JMSException {
//        log.debug("queryMostRecentStatement: {}", loanId);
//        String responseKey = UUID.randomUUID().toString();
//        replyWaitingHandler.put(responseKey);
//        try ( JMSContext jmsContext = connectionFactory.createContext()) {
//            Message message = jmsContext.createObjectMessage(loanId);
//            message.setJMSCorrelationID(responseKey);
//            message.setJMSReplyTo(accountsReplyQueue);
//            jmsContext.createProducer().send(statementQueryMostRecentStatementQueue, message);
//            return replyWaitingHandler.getReply(responseKey);
//        }
//    }
}
