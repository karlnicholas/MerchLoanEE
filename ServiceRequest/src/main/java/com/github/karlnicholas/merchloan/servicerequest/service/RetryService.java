package com.github.karlnicholas.merchloan.servicerequest.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.karlnicholas.merchloan.apimessage.message.ServiceRequestMessage;
import com.github.karlnicholas.merchloan.servicerequest.component.ServiceRequestRouter;
import com.github.karlnicholas.merchloan.servicerequest.model.ServiceRequest;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
@Slf4j
public class RetryService {
    @Inject
    private ServiceRequestRouter serviceRequestRouter;
    private final ObjectMapper objectMapper;

    @Inject
    public RetryService() {
        this.objectMapper = new ObjectMapper().findAndRegisterModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    public void retryServiceRequest(ServiceRequest serviceRequest, String requestType)  {
        try {
            @SuppressWarnings("unchecked")
            Class<? extends ServiceRequestMessage> messageClass = (Class<? extends ServiceRequestMessage>) Class.forName(requestType);
            ServiceRequestMessage serviceRequestMessage = objectMapper.readValue(serviceRequest.getRequest(), messageClass);
            log.debug("retryServiceRequest: {}", serviceRequestMessage);
            serviceRequestRouter.routeRequest(requestType, serviceRequestMessage, Boolean.TRUE, serviceRequest.getId());
        } catch (Exception e) {
            log.error("ERROR in RETRY", e);
        }
    }
}
