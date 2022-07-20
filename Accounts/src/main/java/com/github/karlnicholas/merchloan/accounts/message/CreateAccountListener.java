package com.github.karlnicholas.merchloan.accounts.message;

import com.github.karlnicholas.merchloan.accounts.service.AccountManagementService;
import com.github.karlnicholas.merchloan.jmsmessage.CreateAccount;
import com.github.karlnicholas.merchloan.jmsmessage.ServiceRequestResponse;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.*;

@MessageDriven(name = "CreateAccountMDB", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/AccountCreateAccountQueue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
@Slf4j
public class CreateAccountListener implements MessageListener {
    @Resource(lookup = "java:comp/DefaultJMSConnectionFactory")
    private ConnectionFactory connectionFactory;
    private JMSContext jmsContext;
    private JMSProducer producer;
    @Resource(lookup = "java:global/jms/queue/ServiceRequestResponseQueue")
    private Destination serviceRequestQueue;
    @Inject
    private AccountManagementService accountManagementService;

    @PostConstruct
    public void postConstruct() {
        jmsContext = connectionFactory.createContext();
        producer = jmsContext.createProducer().setDeliveryMode(DeliveryMode.NON_PERSISTENT);
    }
    @PreDestroy
    public void preDestroy() {
        jmsContext.close();
    }
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
            producer.send(serviceRequestQueue, requestResponse);
        }
    }
}
