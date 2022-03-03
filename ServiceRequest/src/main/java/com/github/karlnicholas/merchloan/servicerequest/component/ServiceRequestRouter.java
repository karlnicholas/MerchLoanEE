package com.github.karlnicholas.merchloan.servicerequest.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.karlnicholas.merchloan.apimessage.message.*;
import com.github.karlnicholas.merchloan.jmsmessage.BillingCycleCharge;
import com.github.karlnicholas.merchloan.servicerequest.service.ServiceRequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class ServiceRequestRouter {
    private final Map<String, ExceptionFunction<? super ServiceRequestMessage, UUID>>  routingMap;
    private final ServiceRequestService serviceRequestService;

    public ServiceRequestRouter(ServiceRequestService serviceRequestService) {
        this.serviceRequestService = serviceRequestService;
        routingMap = new HashMap<>();
        routingMap.put(AccountRequest.class.getName(), serviceRequestService::accountRequest);
        routingMap.put(FundingRequest.class.getName(), serviceRequestService::fundingRequest);
        routingMap.put(BillingCycleChargeRequest.class.getName(), serviceRequestService::billingCycleChargeRequest);
        routingMap.put(CloseRequest.class.getName(), serviceRequestService::closeRequest);
        routingMap.put(CreditRequest.class.getName(), serviceRequestService::accountValidateCreditRequest);
        routingMap.put(DebitRequest.class.getName(), serviceRequestService::accountValidateDebitRequest);
    }

    public UUID routeRequest(String clazz, ServiceRequestMessage serviceRequestMessage, Boolean retry) throws JsonProcessingException {
        return routingMap.get(clazz).route(serviceRequestMessage, retry);
    }
}
