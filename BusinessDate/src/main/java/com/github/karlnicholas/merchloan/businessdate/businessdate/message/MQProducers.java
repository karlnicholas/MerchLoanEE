package com.github.karlnicholas.merchloan.businessdate.businessdate.message;

import com.github.karlnicholas.merchloan.jmsmessage.BillingCycle;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.Queue;

@ApplicationScoped
@Slf4j
public class MQProducers {
    @Resource(lookup = "java:jboss/exported/jms/RemoteConnectionFactory")
    private ConnectionFactory connectionFactory;
    @Resource(lookup = "java:global/jms/queue/ServiceRequestBillLoanQueue")
    private Queue serviceRequestBillLoanQueue;

    public void serviceRequestBillLoan(BillingCycle billingCycle) {
        try (JMSContext jmsContext = connectionFactory.createContext()) {
            jmsContext.createProducer().send(serviceRequestBillLoanQueue, jmsContext.createObjectMessage(billingCycle));
        }
    }

}
