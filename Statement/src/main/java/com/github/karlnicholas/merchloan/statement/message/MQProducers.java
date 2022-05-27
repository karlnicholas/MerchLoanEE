package com.github.karlnicholas.merchloan.statement.message;

import com.github.karlnicholas.merchloan.jmsmessage.ServiceRequestResponse;
import com.github.karlnicholas.merchloan.jmsmessage.StatementCompleteResponse;
import com.github.karlnicholas.merchloan.jmsmessage.StatementHeader;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.Queue;

@Slf4j
public class MQProducers {
    @Resource(lookup = "java:jboss/exported/jms/RemoteConnectionFactory")
    private ConnectionFactory connectionFactory;
    @Resource(lookup = "java:global/jms/queue/ServiceRequestQueue")
    private Queue servicerequestQueue;
    @Resource(lookup = "java:global/jms/queue/ServiceRequestQueue")
    private Queue accountLoanClosedQueue;
    @Resource(lookup = "java:global/jms/queue/ServiceRequestQueue")
    private Queue serviceRequestStatementCompleteQueue;

    public void serviceRequestServiceRequest(ServiceRequestResponse serviceRequest) {
        log.debug("serviceRequestServiceRequest: {}", serviceRequest);
        try ( JMSContext jmsContext = connectionFactory.createContext()) {
            jmsContext.createProducer().send(servicerequestQueue, jmsContext.createObjectMessage(serviceRequest));
        }
    }

    public void accountLoanClosed(StatementHeader statementHeader) {
        log.debug("accountLoanClosed: {}", statementHeader);
        try ( JMSContext jmsContext = connectionFactory.createContext()) {
            jmsContext.createProducer().send(accountLoanClosedQueue, jmsContext.createObjectMessage(statementHeader));
        }
    }

    public void serviceRequestStatementComplete(StatementCompleteResponse requestResponse) {
        log.debug("serviceRequestStatementComplete: {}", requestResponse);
        try ( JMSContext jmsContext = connectionFactory.createContext()) {
            jmsContext.createProducer().send(serviceRequestStatementCompleteQueue, jmsContext.createObjectMessage(requestResponse));
        }
    }

}
