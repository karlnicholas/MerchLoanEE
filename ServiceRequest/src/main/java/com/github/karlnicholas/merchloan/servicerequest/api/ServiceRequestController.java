package com.github.karlnicholas.merchloan.servicerequest.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.karlnicholas.merchloan.apimessage.message.*;
import com.github.karlnicholas.merchloan.redis.component.RedisComponent;
import com.github.karlnicholas.merchloan.servicerequest.service.ServiceRequestService;
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
    private final ServiceRequestService serviceRequestService;

    @Autowired
    public ServiceRequestController(ServiceRequestService serviceRequestService) {
        this.serviceRequestService = serviceRequestService;
    }
    @PostMapping(value = "accountRequest", produces = MediaType.TEXT_PLAIN_VALUE)
    public UUID createAccountRequest(@RequestBody AccountRequest accountRequest) throws JsonProcessingException {
        return serviceRequestService.accountRequest(accountRequest);
    }
    @PostMapping(value = "fundingRequest", produces = MediaType.TEXT_PLAIN_VALUE)
    public UUID fundingRequest(@RequestBody FundingRequest fundingRequest) throws JsonProcessingException {
        return serviceRequestService.fundingRequest(fundingRequest);
    }
    @PostMapping(value = "creditRequest", produces = MediaType.TEXT_PLAIN_VALUE)
    public UUID creditRequest(@RequestBody CreditRequest creditRequest) throws JsonProcessingException {
        return serviceRequestService.accountValidateCreditRequest(creditRequest);
    }
    @PostMapping(value = "debitRequest", produces = MediaType.TEXT_PLAIN_VALUE)
    public UUID debitRequest(@RequestBody DebitRequest debitRequest) throws JsonProcessingException {
        return serviceRequestService.accountValidateDebitRequest(debitRequest);
    }
    @PostMapping(value = "statementRequest", produces = MediaType.APPLICATION_JSON_VALUE)
    public UUID statementRequest(@RequestBody StatementRequest statementRequest) throws JsonProcessingException {
        return serviceRequestService.statementStatementRequest(statementRequest);
    }
}