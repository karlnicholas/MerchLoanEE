package com.github.karlnicholas.merchloan.servicerequest.component;

import com.github.karlnicholas.merchloan.apimessage.message.*;
import com.github.karlnicholas.merchloan.servicerequest.service.ServiceRequestService;
import jakarta.jms.Connection;
import jakarta.jms.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class ServiceRequestRouter {
    private final Map<String, ExceptionFunction<? super ServiceRequestMessage, UUID>>  routingMap;


    public ServiceRequestRouter(ServiceRequestService serviceRequestService) {
        routingMap = new HashMap<>();
        routingMap.put(AccountRequest.class.getName(), serviceRequestService::accountRequest);
        routingMap.put(FundingRequest.class.getName(), serviceRequestService::fundingRequest);
        routingMap.put(CloseRequest.class.getName(), serviceRequestService::closeRequest);
        routingMap.put(CreditRequest.class.getName(), serviceRequestService::accountValidateCreditRequest);
        routingMap.put(DebitRequest.class.getName(), serviceRequestService::accountValidateDebitRequest);
        routingMap.put(StatementRequest.class.getName(), serviceRequestService::statementStatementRequest);
    }

    public UUID routeRequest(String clazz, ServiceRequestMessage serviceRequestMessage, Boolean retry, UUID existingId) throws Exception {
        return routingMap.get(clazz).route(serviceRequestMessage, retry, existingId);
    }
}
