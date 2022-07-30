package com.github.karlnicholas.merchloan.accounts.message;

import com.github.karlnicholas.merchloan.accounts.service.AccountManagementService;
import com.github.karlnicholas.merchloan.jmsmessage.CreateAccount;
import com.github.karlnicholas.merchloan.jmsmessage.ServiceRequestResponse;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.*;

@MessageDriven(name = "CreateAccountMDB", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/AccountsCreateAccountQueue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
@Slf4j
public class CreateAccountListener implements MessageListener {
    @Inject
    private JMSContext jmsContext;
    @Resource(lookup = "java:global/jms/queue/ServiceRequestResponseQueue")
    private Destination serviceRequestQueue;
    @Inject
    private AccountManagementService accountManagementService;

    @Override
    public void onMessage(Message message) {
        CreateAccount createAccount = null;
        try {
            createAccount = (CreateAccount) ((ObjectMessage) message).getObject();
        } catch (JMSException e) {
            log.error("receivedCreateAccountMessage exception {}", e);
            return;
        }
        ServiceRequestResponse requestResponse = ServiceRequestResponse.builder().id(createAccount.getId()).build();
        try {
            log.debug("receivedCreateAccountMessage: {}", createAccount);
            accountManagementService.createAccount(createAccount, requestResponse);
        } catch (Exception ex) {
            log.error("receivedCreateAccountMessage exception {}", ex.getMessage());
            requestResponse.setError(ex.getMessage());
        } finally {
            JMSProducer p = jmsContext.createProducer();
            p.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            p.send(serviceRequestQueue, jmsContext.createObjectMessage(requestResponse));
        }
    }
}
