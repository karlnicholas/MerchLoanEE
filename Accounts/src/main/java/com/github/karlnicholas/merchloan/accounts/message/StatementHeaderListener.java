package com.github.karlnicholas.merchloan.accounts.message;

import com.github.karlnicholas.merchloan.accounts.service.AccountManagementService;
import com.github.karlnicholas.merchloan.accounts.service.RegisterManagementService;
import com.github.karlnicholas.merchloan.jmsmessage.ServiceRequestResponse;
import com.github.karlnicholas.merchloan.jmsmessage.StatementHeader;
import lombok.extern.slf4j.Slf4j;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.*;

@MessageDriven(name = "StatementHeaderMDB", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/AccountsStatementHeaderQueue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
@Slf4j
public class StatementHeaderListener implements MessageListener {
    @Inject
    private JMSContext jmsContext;
    @Inject
    private AccountManagementService accountManagementService;
    @Inject
    private RegisterManagementService registerManagementService;

    @Override
    public void onMessage(Message message) {
        try {
            StatementHeader statementHeader = message.getBody(StatementHeader.class);
            log.debug("receivedStatementHeaderMessage {}", statementHeader);
            ServiceRequestResponse serviceRequestResponse = accountManagementService.statementHeader(statementHeader);
            if (serviceRequestResponse.isSuccess())
                registerManagementService.setStatementHeaderRegisterEntryies(statementHeader);
            jmsContext.createProducer()
                    .setDeliveryMode(DeliveryMode.NON_PERSISTENT)
                    .setJMSCorrelationID(message.getJMSCorrelationID())
                    .send(message.getJMSReplyTo(), jmsContext.createObjectMessage(statementHeader));
        } catch (Exception ex) {
            log.error("receivedStatementHeaderMessage exception {}", ex.getMessage());
            throw new EJBException(ex);
        }
    }
}
