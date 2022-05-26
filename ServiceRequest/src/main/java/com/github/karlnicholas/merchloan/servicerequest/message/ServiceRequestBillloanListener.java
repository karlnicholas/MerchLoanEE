package com.github.karlnicholas.merchloan.servicerequest.message;

import com.github.karlnicholas.merchloan.apimessage.message.StatementRequest;
import com.github.karlnicholas.merchloan.jmsmessage.BillingCycle;
import com.github.karlnicholas.merchloan.servicerequest.component.ServiceRequestException;
import com.github.karlnicholas.merchloan.servicerequest.service.ServiceRequestService;
import lombok.extern.slf4j.Slf4j;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.*;

@JMSDestinationDefinition(
        name = "java:global/jms/queue/ServiceRequestBillloanQueue",
        interfaceName = "javax.jms.Queue",
        destinationName = "ServiceRequestBillloanQueue"
)
@MessageDriven(name = "ServiceRequestBillloanMDB", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/ServiceRequestBillloanQueue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
@Slf4j
public class ServiceRequestBillloanListener implements MessageListener {
    private final ServiceRequestService serviceRequestService;

    @Inject
    public ServiceRequestBillloanListener(ServiceRequestService serviceRequestService) {
        this.serviceRequestService = serviceRequestService;
    }

    @Override
    public void onMessage(Message message) {
        try {
            BillingCycle billingCycle = (BillingCycle) ((ObjectMessage) message).getObject();
            log.debug("ServiceRequestBillloan: {}", billingCycle);
            serviceRequestService.statementStatementRequest(StatementRequest.builder()
                            .loanId(billingCycle.getLoanId())
                            .statementDate(billingCycle.getStatementDate())
                            .startDate(billingCycle.getStartDate())
                            .endDate(billingCycle.getEndDate())
                            .build(),
                    Boolean.FALSE, null);
        } catch (ServiceRequestException | JMSException ex) {
            log.error("ServiceRequestBillloan", ex);
        }

    }
}
