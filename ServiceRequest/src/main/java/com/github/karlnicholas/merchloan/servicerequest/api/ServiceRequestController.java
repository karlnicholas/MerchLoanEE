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

@RestController
@RequestMapping(value = "/api/v1/service/")
public class ServiceRequestController {
    private final ServiceRequestRouter serviceRequestRouter;

    @Autowired
    public ServiceRequestController(ServiceRequestRouter serviceRequestRouter) {
        this.serviceRequestRouter = serviceRequestRouter;
    }
    @PostMapping(value = "accountRequest", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String createAccountRequest(@RequestBody AccountRequest accountRequest) throws JsonProcessingException {
        return serviceRequestRouter.routeRequest(accountRequest.getClass().getName(), accountRequest, Boolean.FALSE, null).toString();
    }
    @PostMapping(value = "fundingRequest", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String fundingRequest(@RequestBody FundingRequest fundingRequest) throws JsonProcessingException {
        return serviceRequestRouter.routeRequest(fundingRequest.getClass().getName(), fundingRequest, Boolean.FALSE, null).toString();
    }
    @PostMapping(value = "creditRequest", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String creditRequest(@RequestBody CreditRequest creditRequest) throws JsonProcessingException {
        return serviceRequestRouter.routeRequest(creditRequest.getClass().getName(), creditRequest, Boolean.FALSE, null).toString();
    }
    @PostMapping(value = "debitRequest", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String debitRequest(@RequestBody DebitRequest debitRequest) throws JsonProcessingException {
        return serviceRequestRouter.routeRequest(debitRequest.getClass().getName(), debitRequest, Boolean.FALSE, null).toString();
    }
    @PostMapping(value = "statementRequest", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String statementRequest(@RequestBody StatementRequest statementRequest) throws JsonProcessingException {
        return serviceRequestRouter.routeRequest(statementRequest.getClass().getName(), statementRequest, Boolean.FALSE, null).toString();
    }
    @PostMapping(value = "closeRequest", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String fundingRequest(@RequestBody CloseRequest closeRequest) throws JsonProcessingException {
        return serviceRequestRouter.routeRequest(closeRequest.getClass().getName(), closeRequest, Boolean.FALSE, null).toString();
    }
}