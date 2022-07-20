package com.github.karlnicholas.merchloan.accounts.message;

import com.github.karlnicholas.merchloan.accounts.service.AccountManagementService;
import com.github.karlnicholas.merchloan.jmsmessage.ServiceRequestResponse;
import com.github.karlnicholas.merchloan.jmsmessage.StatementHeader;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.*;

@MessageDriven(name = "LoanClosedMDB", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/AccountLoanClosedQueue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge")})
@Slf4j
public class LoanClosedListener implements MessageListener {
    @Inject
    private JMSContext jmsContext;
    @Resource(lookup = "java:global/jms/queue/ServiceRequestResponseQueue")
    private Destination serviceRequestQueue;
    @Inject
    private AccountManagementService accountManagementService;

    @Override
    public void onMessage(Message message) {
        StatementHeader statementHeader = null;
        try {
            statementHeader = (StatementHeader) ((ObjectMessage) message).getObject();
        } catch (JMSException e) {
            log.error("receivedLoanClosedMessage exception", e);
            return;
        }
        ServiceRequestResponse serviceRequestResponse = ServiceRequestResponse.builder().id(statementHeader.getId()).build();
        try {
            log.debug("receivedLoanClosedMessage: {} ", statementHeader);
            accountManagementService.closeLoan(statementHeader.getLoanId());
            serviceRequestResponse.setSuccess();
        } catch (Exception ex) {
            log.error("receivedLoanClosedMessage exception", ex);
            serviceRequestResponse.setError("receivedLoanClosedMessage exception: " + ex.getMessage());
        } finally {
            jmsContext.createProducer().setDeliveryMode(DeliveryMode.NON_PERSISTENT).send(serviceRequestQueue, serviceRequestResponse);
        }
    }
}
