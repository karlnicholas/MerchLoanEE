package com.github.karlnicholas.merchloan.statement.message;

import com.github.karlnicholas.merchloan.jmsmessage.ServiceRequestResponse;
import com.github.karlnicholas.merchloan.jmsmessage.StatementCompleteResponse;
import com.github.karlnicholas.merchloan.jmsmessage.StatementHeader;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSContext;
import javax.jms.Queue;

@Slf4j
@ApplicationScoped
public class MQProducers {
    @Resource(lookup = "java:comp/DefaultJMSConnectionFactory")
    private ConnectionFactory connectionFactory;
    @Resource(lookup = "java:global/jms/queue/ServiceRequestResponseQueue")
    private Queue servicerequestQueue;
    @Resource(lookup = "java:global/jms/queue/AccountLoanClosedQueue")
    private Queue accountLoanClosedQueue;
    @Resource(lookup = "java:global/jms/queue/ServiceRequestStatementCompleteQueue")
    private Queue serviceRequestStatementCompleteQueue;

    public void serviceRequestServiceRequest(ServiceRequestResponse serviceRequest) {
        log.debug("serviceRequestServiceRequest: {}", serviceRequest);
        try ( JMSContext jmsContext = connectionFactory.createContext()) {
            jmsContext.createProducer().setDeliveryMode(DeliveryMode.NON_PERSISTENT).send(servicerequestQueue, jmsContext.createObjectMessage(serviceRequest));
        }
    }

    public void accountLoanClosed(StatementHeader statementHeader) {
        log.debug("accountLoanClosed: {}", statementHeader);
        try ( JMSContext jmsContext = connectionFactory.createContext()) {
            jmsContext.createProducer().setDeliveryMode(DeliveryMode.NON_PERSISTENT).send(accountLoanClosedQueue, jmsContext.createObjectMessage(statementHeader));
        }
    }

    public void serviceRequestStatementComplete(StatementCompleteResponse requestResponse) {
        log.debug("serviceRequestStatementComplete: {}", requestResponse);
        try ( JMSContext jmsContext = connectionFactory.createContext()) {
            jmsContext.createProducer().setDeliveryMode(DeliveryMode.NON_PERSISTENT).send(serviceRequestStatementCompleteQueue, jmsContext.createObjectMessage(requestResponse));
        }
    }

}
