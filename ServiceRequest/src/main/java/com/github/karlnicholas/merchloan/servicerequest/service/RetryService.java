package com.github.karlnicholas.merchloan.servicerequest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.karlnicholas.merchloan.apimessage.message.ServiceRequestMessage;
import com.github.karlnicholas.merchloan.servicerequest.component.ServiceRequestRouter;
import com.github.karlnicholas.merchloan.servicerequest.model.ServiceRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RetryService {
    private final ServiceRequestService serviceRequestService;
    private final ServiceRequestRouter serviceRequestRouter;
    private final ObjectMapper objectMapper;

    public RetryService(ServiceRequestService serviceRequestService, ServiceRequestRouter serviceRequestRouter, ObjectMapper objectMapper) {
        this.serviceRequestService = serviceRequestService;
        this.serviceRequestRouter = serviceRequestRouter;
        this.objectMapper = objectMapper;
    }

    @Async
    public void retryServiceRequest(ServiceRequest serviceRequest)  {
        try {
            ServiceRequestMessage serviceRequestMessage = objectMapper.readValue(serviceRequest.getRequest(), ServiceRequestMessage.class);
            serviceRequestRouter.routeRequest(serviceRequest.getRequest(), serviceRequestMessage, Boolean.TRUE);
        } catch (Exception e) {
            log.error("ERROR in RETRY", e);
        }
    }
}
