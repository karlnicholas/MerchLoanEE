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
    private final Connection connection;
    private final ReplyWaitingHandler replyWaitingHandler;
    private final Queue businessDateReplyQueue;
    private final Destination serviceRequestCheckRequestQueue;
    private final Destination accountQueryLoansToCycleQueue;
    private final Destination serviceRequestBillLoanQueue;
    @Autowired
    public MQProducers(Connection connection, MQConsumerUtils mqConsumerUtils) throws JMSException {
        this.connection = connection;
        connection.setClientID("BusinessDate");
        try ( Session session = connection.createSession()) {
            replyWaitingHandler = new ReplyWaitingHandler();
            serviceRequestCheckRequestQueue = session.createQueue(mqConsumerUtils.getServiceRequestCheckRequestQueue());
            accountQueryLoansToCycleQueue = session.createQueue(mqConsumerUtils.getAccountQueryLoansToCycleQueue());
            serviceRequestBillLoanQueue = session.createQueue(mqConsumerUtils.getServiceRequestBillLoanQueue());
        }
        Session consumerSession = connection.createSession();
        businessDateReplyQueue = consumerSession.createTemporaryQueue();
        mqConsumerUtils.bindConsumer(consumerSession, businessDateReplyQueue, replyWaitingHandler::onMessage);
        connection.start();
    }

    public Object servicerequestCheckRequest() throws InterruptedException, JMSException {
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        try (Session session = connection.createSession()) {
            Message message = session.createObjectMessage(new byte[0]);
            message.setJMSCorrelationID(responseKey);
            message.setJMSReplyTo(businessDateReplyQueue);
            try (MessageProducer producer = session.createProducer(serviceRequestCheckRequestQueue)) {
                producer.send(message);
                return replyWaitingHandler.getReply(responseKey);
            }
        }
    }

    public Object acccountQueryLoansToCycle(LocalDate businessDate) throws InterruptedException, JMSException {
        String responseKey = UUID.randomUUID().toString();
        replyWaitingHandler.put(responseKey);
        try (Session session = connection.createSession()) {
            Message message = session.createObjectMessage(businessDate);
            message.setJMSCorrelationID(responseKey);
            message.setJMSReplyTo(businessDateReplyQueue);
            try (MessageProducer producer = session.createProducer(accountQueryLoansToCycleQueue)) {
                producer.send(message);
                return replyWaitingHandler.getReply(responseKey);
            }
        }
    }

    public void serviceRequestBillLoan(BillingCycle billingCycle) throws JMSException {
        try (Session session = connection.createSession()) {
            try (MessageProducer producer = session.createProducer(serviceRequestBillLoanQueue)) {
                producer.send(session.createObjectMessage(billingCycle));
            }
        }
    }

}
