package com.github.karlnicholas.merchloan.servicerequest.message;

import com.github.karlnicholas.merchloan.jmsmessage.StatementCompleteResponse;
import com.github.karlnicholas.merchloan.servicerequest.service.ServiceRequestService;
import lombok.extern.slf4j.Slf4j;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.*;
import java.sql.SQLException;

@JMSDestinationDefinition(
        name = "java:global/jms/queue/ServiceRequestStatementCompleteQueue",
        interfaceName = "javax.jms.Queue",
        destinationName = "ServiceRequestStatementCompleteQueue"
)
@MessageDriven(name = "ServiceRequestStatementCompleteMDB", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/ServiceRequestStatementCompleteQueue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
@Slf4j
public class ServiceRequestStatementCompleteListener implements MessageListener {
    @Inject
    private ServiceRequestService serviceRequestService;


    @Override
    public void onMessage(Message message) {
        try {
            StatementCompleteResponse statementCompleteResponse = (StatementCompleteResponse) ((ObjectMessage) message).getObject();
            log.debug("ServiceRequestStatementComplete: {}", statementCompleteResponse);
            serviceRequestService.statementComplete(statementCompleteResponse);
        } catch (SQLException | JMSException ex) {
            log.error("ServiceRequestStatementComplete", ex);
        }
    }
}
