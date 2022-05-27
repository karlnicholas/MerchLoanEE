package com.github.karlnicholas.merchloan.servicerequest.component;

import com.github.karlnicholas.merchloan.apimessage.message.ServiceRequestMessage;
import com.github.karlnicholas.merchloan.servicerequest.service.ServiceRequestService;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
@Slf4j
public class ServiceRequestRouter {
    private final Map<String, ExceptionFunction<? super ServiceRequestMessage, UUID>>  routingMap;
    @Inject
    private ServiceRequestService serviceRequestService;

    @Inject
    public ServiceRequestRouter() {
        routingMap = new HashMap<>();
//        routingMap.put(AccountRequest.class.getName(), serviceRequestService::accountRequest);
//        routingMap.put(FundingRequest.class.getName(), serviceRequestService::fundingRequest);
//        routingMap.put(CloseRequest.class.getName(), serviceRequestService::closeRequest);
//        routingMap.put(CreditRequest.class.getName(), serviceRequestService::accountValidateCreditRequest);
//        routingMap.put(DebitRequest.class.getName(), serviceRequestService::accountValidateDebitRequest);
//        routingMap.put(StatementRequest.class.getName(), serviceRequestService::statementStatementRequest);
    }

    public UUID routeRequest(String clazz, ServiceRequestMessage serviceRequestMessage, Boolean retry, UUID existingId) throws Exception {
        return routingMap.get(clazz).route(serviceRequestMessage, retry, existingId);
    }
}
