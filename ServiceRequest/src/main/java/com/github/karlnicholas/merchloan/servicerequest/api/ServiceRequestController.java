package com.github.karlnicholas.merchloan.servicerequest.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.karlnicholas.merchloan.apimessage.message.*;
import com.github.karlnicholas.merchloan.servicerequest.component.ServiceRequestRouter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/service/")
public class ServiceRequestController {
    private final ServiceRequestRouter serviceRequestRouter;

    @Autowired
    public ServiceRequestController(ServiceRequestRouter serviceRequestRouter) {
        this.serviceRequestRouter = serviceRequestRouter;
    }
    @PostMapping(value = "accountRequest", produces = MediaType.TEXT_PLAIN_VALUE)
    public UUID createAccountRequest(@RequestBody AccountRequest accountRequest) throws JsonProcessingException {
        return serviceRequestRouter.routeRequest(accountRequest.getClass().getName(), accountRequest, Boolean.FALSE);
    }
    @PostMapping(value = "fundingRequest", produces = MediaType.TEXT_PLAIN_VALUE)
    public UUID fundingRequest(@RequestBody FundingRequest fundingRequest) throws JsonProcessingException {
        return serviceRequestRouter.routeRequest(fundingRequest.getClass().getName(), fundingRequest, Boolean.FALSE);
    }
    @PostMapping(value = "creditRequest", produces = MediaType.TEXT_PLAIN_VALUE)
    public UUID creditRequest(@RequestBody CreditRequest creditRequest) throws JsonProcessingException {
        return serviceRequestRouter.routeRequest(creditRequest.getClass().getName(), creditRequest, Boolean.FALSE);
    }
    @PostMapping(value = "debitRequest", produces = MediaType.TEXT_PLAIN_VALUE)
    public UUID debitRequest(@RequestBody DebitRequest debitRequest) throws JsonProcessingException {
        return serviceRequestRouter.routeRequest(debitRequest.getClass().getName(), debitRequest, Boolean.FALSE);
    }
    @PostMapping(value = "statementRequest", produces = MediaType.APPLICATION_JSON_VALUE)
    public UUID statementRequest(@RequestBody StatementRequest statementRequest) throws JsonProcessingException {
        return serviceRequestRouter.routeRequest(statementRequest.getClass().getName(), statementRequest, Boolean.FALSE);
    }
    @PostMapping(value = "closeRequest", produces = MediaType.TEXT_PLAIN_VALUE)
    public UUID fundingRequest(@RequestBody CloseRequest closeRequest) throws JsonProcessingException {
        return serviceRequestRouter.routeRequest(closeRequest.getClass().getName(), closeRequest, Boolean.FALSE);
    }
}