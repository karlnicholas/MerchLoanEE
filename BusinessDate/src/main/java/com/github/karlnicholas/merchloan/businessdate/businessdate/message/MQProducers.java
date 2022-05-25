package com.github.karlnicholas.merchloan.businessdate.businessdate.message;

import com.github.karlnicholas.merchloan.jms.MQConsumerUtils;
import com.github.karlnicholas.merchloan.jms.ReplyWaitingHandler;
import com.github.karlnicholas.merchloan.jmsmessage.BillingCycle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.jms.*;
import java.time.LocalDate;
import java.util.UUID;

@Service
@Slf4j
public class MQProducers {
    private final JMSContext serviceRequestCheckRequestContext;
    private final Destination serviceRequestCheckRequestQueue;
    private final JMSContext accountQueryLoansToCycleContext;
    private final Destination accountQueryLoansToCycleQueue;
    private final JMSContext serviceRequestBillLoanContext;
    private final Destination serviceRequestBillLoanQueue;

    private final ReplyWaitingHandler replyWaitingHandler;
    private final JMSContext businessDateReplyContext;
    private final Destination businessDateReplyQueue;

    @Autowired
    public MQProducers(ConnectionFactory connectionFactory, MQConsumerUtils mqConsumerUtils) {

        serviceRequestCheckRequestContext = connectionFactory.createContext();
        serviceRequestCheckRequestContext.setClientID("BusinessDate::serviceRequestCheckRequestContext");
        serviceRequestCheckRequestQueue = serviceRequestCheckRequestContext.createQueue(mqConsumerUtils.getServiceRequestCheckRequestQueue());
        accountQueryLoansToCycleContext = connectionFactory.createContext();
        accountQueryLoansToCycleContext.setClientID("BusinessDate::accountQueryLoansToCycleContext");
        accountQueryLoansToCycleQueue = accountQueryLoansToCycleContext.createQueue(mqConsumerUtils.getAccountQueryLoansToCycleQueue());
        serviceRequestBillLoanContext = connectionFactory.createContext();
        serviceRequestBillLoanContext.setClientID("BusinessDate::serviceRequestBillLoanContext");
        serviceRequestBillLoanQueue = serviceRequestBillLoanContext.createQueue(mqConsumerUtils.getServiceRequestBillLoanQueue());

        replyWaitingHandler = new ReplyWaitingHandler();
        businessDateReplyContext = connectionFactory.createContext();
        businessDateReplyContext.setClientID("BusinessDate::businessDateReplyContext");
        businessDateReplyQueue = businessDateReplyContext.createTemporaryQueue();
        businessDateReplyContext.createConsumer(businessDateReplyQueue).setMessageListener(replyWaitingHandler::onMessage);
    }

    public Object servicerequestCheckRequest() throws InterruptedException, JMSException {
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        Message message = serviceRequestCheckRequestContext.createObjectMessage(new byte[0]);
        message.setJMSCorrelationID(responseKey);
        message.setJMSReplyTo(businessDateReplyQueue);
        serviceRequestCheckRequestContext.createProducer().send(serviceRequestCheckRequestQueue, message);
        return replyWaitingHandler.getReply(responseKey);
    }

    public Object acccountQueryLoansToCycle(LocalDate businessDate) throws InterruptedException, JMSException {
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        Message message = accountQueryLoansToCycleContext.createObjectMessage(businessDate);
        message.setJMSCorrelationID(responseKey);
        message.setJMSReplyTo(businessDateReplyQueue);
        accountQueryLoansToCycleContext.createProducer().send(accountQueryLoansToCycleQueue, message);
        return replyWaitingHandler.getReply(responseKey);
    }

    public void serviceRequestBillLoan(BillingCycle billingCycle) {
        serviceRequestBillLoanContext.createProducer().send(serviceRequestBillLoanQueue, serviceRequestBillLoanContext.createObjectMessage(billingCycle));
    }

}
