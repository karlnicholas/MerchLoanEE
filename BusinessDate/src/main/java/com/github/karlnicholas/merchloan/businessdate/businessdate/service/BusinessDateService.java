package com.github.karlnicholas.merchloan.businessdate.businessdate.service;

import com.github.karlnicholas.merchloan.businessdate.businessdate.dao.BusinessDateDao;
import com.github.karlnicholas.merchloan.businessdate.businessdate.model.BusinessDate;
import com.github.karlnicholas.merchloan.jmsmessage.BillingCycle;
import com.github.karlnicholas.merchloan.redis.component.RedisComponent;
import com.github.karlnicholas.merchloan.replywaiting.ReplyWaitingHandler;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.*;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
@Slf4j
public class BusinessDateService {
    @Inject
    private JMSContext jmsContext;
    @Resource(lookup = "java:global/jms/queue/ServiceRequestBillLoanQueue")
    private Queue serviceRequestBillLoanQueue;
    @Resource(lookup = "java:jboss/datasources/BusinessDateDS")
    private DataSource dataSource;
    @Inject
    private RedisComponent redisComponent;
    @Inject
    private BusinessDateDao businessDateDao;
    @Resource(lookup = "java:global/jms/queue/ServiceRequestCheckRequestQueue")
    private Queue serviceRequestCheckRequestQueue;
    private TemporaryQueue checkRequestReplyQueue;
    private ReplyWaitingHandler replyWaitingHandlerCheckRequest;
    @Resource(lookup = "java:global/jms/queue/AccountsLoansToCycleQueue")
    private Queue accountsLoansToCycleQueue;
    private TemporaryQueue loansToCycleReplyQueue;
    private ReplyWaitingHandler replyWaitingHandlerLoansToCycle;

    @PostConstruct
    public void postConstruct() {
        replyWaitingHandlerCheckRequest = new ReplyWaitingHandler();
        checkRequestReplyQueue = jmsContext.createTemporaryQueue();
        JMSConsumer checkRequestReplyConsumer = jmsContext.createConsumer(checkRequestReplyQueue);
        checkRequestReplyConsumer.setMessageListener(m -> {
            try {
                replyWaitingHandlerCheckRequest.handleReply(m.getJMSCorrelationID(), m.getBody(Object.class));
            } catch (JMSException e) {
                log.error("replyWaitingHandlerCheckRequest ", e);
            }
        });

        replyWaitingHandlerLoansToCycle = new ReplyWaitingHandler();
        loansToCycleReplyQueue = jmsContext.createTemporaryQueue();
        JMSConsumer loansToCycleReplyConsumer = jmsContext.createConsumer(accountsLoansToCycleQueue);
        loansToCycleReplyConsumer.setMessageListener(m -> {
            try {
                replyWaitingHandlerLoansToCycle.handleReply(m.getJMSCorrelationID(), m.getBody(Object.class));
            } catch (JMSException e) {
                log.error("replyWaitingHandlerCheckRequest ", e);
            }
        });
    }

    public void updateBusinessDate(LocalDate businessDate) throws SQLException, JMSException, InterruptedException {
        try (Connection con = dataSource.getConnection()) {
            Optional<BusinessDate> existingBusinessDate = businessDateDao.findById(con, 1L);
            if (existingBusinessDate.isPresent()) {
                Instant start = Instant.now();
                BusinessDate priorBusinessDate = BusinessDate.builder().date(existingBusinessDate.get().getDate()).build();

                JMSProducer producer = jmsContext.createProducer();
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
                ObjectMessage message = jmsContext.createObjectMessage(new byte[0]);
                String correlationId = UUID.randomUUID().toString();
                message.setJMSReplyTo(checkRequestReplyQueue);
                message.setJMSCorrelationID(correlationId);
                replyWaitingHandlerCheckRequest.put(correlationId);
                producer.send(serviceRequestCheckRequestQueue, message);
                Boolean stillProcessing = (Boolean) replyWaitingHandlerCheckRequest.getReply(correlationId);
//                Boolean stillProcessing = serviceRequestEjb.checkRequest();
                if (stillProcessing == null || stillProcessing) {
                    throw new java.lang.IllegalStateException("Still processing prior business date" + priorBusinessDate.getDate());
                }
                businessDateDao.updateDate(con, BusinessDate.builder().id(1L).date(businessDate).build());
                redisComponent.updateBusinessDate(businessDate);
                startBillingCycle(priorBusinessDate.getDate());
                log.info("updateBusinessDate {} {}", businessDate, Duration.between(start, Instant.now()));
            } else {
                throw new java.lang.IllegalStateException("Business Date No State: " + businessDate);
            }
        }
    }

    public void initializeBusinessDate() throws SQLException {
        try (Connection con = dataSource.getConnection()) {
            Optional<BusinessDate> businessDate = businessDateDao.findById(con, 1L);
            BusinessDate currentBd = BusinessDate.builder().id(1L).date(LocalDate.now()).build();
            if ( businessDate.isPresent()) {
                businessDateDao.updateDate(con, currentBd);
            } else {
                businessDateDao.insert(con, currentBd);
            }
            redisComponent.updateBusinessDate(currentBd.getDate());
        }
    }

    public void startBillingCycle(LocalDate priorBusinessDate) throws JMSException, InterruptedException {
//        List<BillingCycle> loansToCycle = accountsEjb.loansToCycle(priorBusinessDate);
        JMSProducer producer = jmsContext.createProducer();
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        ObjectMessage message = jmsContext.createObjectMessage(priorBusinessDate);
        String correlationId = UUID.randomUUID().toString();
        message.setJMSReplyTo(loansToCycleReplyQueue);
        message.setJMSCorrelationID(correlationId);
        replyWaitingHandlerCheckRequest.put(correlationId);
        producer.send(accountsLoansToCycleQueue, message);
        List<BillingCycle> loansToCycle = (List<BillingCycle>) replyWaitingHandlerCheckRequest.getReply(correlationId);

        for( BillingCycle billingCycle: loansToCycle) {
            jmsContext.createProducer().setDeliveryMode(DeliveryMode.NON_PERSISTENT).send(serviceRequestBillLoanQueue, jmsContext.createObjectMessage(billingCycle));
        }
    }
}
