package com.github.karlnicholas.merchloan.businessdate.businessdate.message;

import com.github.karlnicholas.merchloan.jms.MQConsumerUtils;
import com.github.karlnicholas.merchloan.jms.ReplyWaitingHandler;
import com.github.karlnicholas.merchloan.jmsmessage.BillingCycle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jms.*;
import java.time.LocalDate;
import java.util.UUID;

@Service
@Slf4j
public class MQProducers {
    private final Session session;
    private final MessageProducer businessDateProducer;
    private final ReplyWaitingHandler replyWaitingHandler;
    private final Destination businessDateReplyQueue;
    private final Destination serviceRequestCheckRequestQueue;
    private final Destination accountQueryLoansToCycleQueue;
    private final Destination serviceRequestBillLoanQueue;
    @Autowired
    public MQProducers(Session session, MQConsumerUtils mqConsumerUtils) throws JMSException {
        this.session = session;
        businessDateReplyQueue = session.createTemporaryQueue();
        replyWaitingHandler = new ReplyWaitingHandler();
        businessDateProducer = session.createProducer(null);
        serviceRequestCheckRequestQueue = session.createQueue(mqConsumerUtils.getServiceRequestCheckRequestQueue());
        accountQueryLoansToCycleQueue = session.createQueue(mqConsumerUtils.getAccountQueryLoansToCycleQueue());
        serviceRequestBillLoanQueue = session.createQueue(mqConsumerUtils.getServiceRequestBillLoanQueue());
        mqConsumerUtils.bindConsumer(session, businessDateReplyQueue, replyWaitingHandler::onMessage);
    }

    public Object servicerequestCheckRequest() throws InterruptedException, JMSException {
        log.debug("servicerequestCheckRequest:");
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        Message message = session.createObjectMessage(new byte[0]);
        message.setJMSCorrelationID(responseKey);
        message.setJMSReplyTo(businessDateReplyQueue);
        businessDateProducer.send(serviceRequestCheckRequestQueue, message);
        return replyWaitingHandler.getReply(responseKey);
    }

    public Object acccountQueryLoansToCycle(LocalDate businessDate) throws InterruptedException, JMSException {
        log.debug("acccountQueryLoansToCycle: {}", businessDate);
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        Message message = session.createObjectMessage(businessDate);
        message.setJMSCorrelationID(responseKey);
        message.setJMSReplyTo(businessDateReplyQueue);
        businessDateProducer.send(accountQueryLoansToCycleQueue, message);
        return replyWaitingHandler.getReply(responseKey);
    }

    public void serviceRequestBillLoan(BillingCycle billingCycle) throws JMSException {
        businessDateProducer.send(serviceRequestBillLoanQueue, session.createObjectMessage(billingCycle));
    }

}
