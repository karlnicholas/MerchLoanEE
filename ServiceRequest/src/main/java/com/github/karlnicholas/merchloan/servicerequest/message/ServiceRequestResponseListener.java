package com.github.karlnicholas.merchloan.servicerequest.message;

import com.github.karlnicholas.merchloan.servicerequest.service.ServiceRequestService;
import lombok.extern.slf4j.Slf4j;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSDestinationDefinition;
import javax.jms.Message;
import javax.jms.MessageListener;

@JMSDestinationDefinition(
        name = "java:global/jms/queue/ServiceRequestResponseQueue",
        interfaceName = "javax.jms.Queue",
        destinationName = "ServiceRequestResponseQueue"
)
@MessageDriven(name = "ServiceRequestResponseMDB", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/ServiceRequestResponseQueue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
@Slf4j
public class ServiceRequestResponseListener implements MessageListener {
    @Inject
    private ServiceRequestService serviceRequestService;

    @Override
    public void onMessage(Message message) {
//        try {
//            ServiceRequestResponse serviceRequest = (ServiceRequestResponse) ((ObjectMessage) message).getObject();
//            log.debug("ServiceRequestResponse {}", serviceRequest);
//            serviceRequestService.completeServiceRequest(serviceRequest);
//        } catch (SQLException | JMSException ex) {
//            log.error("ServiceRequestResponse", ex);
//        }
    }
}
