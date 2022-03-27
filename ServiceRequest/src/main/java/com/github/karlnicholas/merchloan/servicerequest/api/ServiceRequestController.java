package com.github.karlnicholas.merchloan.servicerequest.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.karlnicholas.merchloan.apimessage.message.*;
import com.github.karlnicholas.merchloan.servicerequest.component.ServiceRequestRouter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/service/")
@Slf4j
public class ServiceRequestController {
    private final ServiceRequestRouter serviceRequestRouter;

    @Autowired
    public ServiceRequestController(ServiceRequestRouter serviceRequestRouter) {
        this.serviceRequestRouter = serviceRequestRouter;
    }
    @PostMapping(value = "accountRequest", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String createAccountRequest(@RequestBody AccountRequest accountRequest) throws JsonProcessingException {
        log.debug("accountRequest: {}", accountRequest);
        return serviceRequestRouter.routeRequest(accountRequest.getClass().getName(), accountRequest, Boolean.FALSE, null).toString();
    }
    @PostMapping(value = "fundingRequest", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String fundingRequest(@RequestBody FundingRequest fundingRequest) throws JsonProcessingException {
        log.debug("fundingRequest: {}", fundingRequest);
        return serviceRequestRouter.routeRequest(fundingRequest.getClass().getName(), fundingRequest, Boolean.FALSE, null).toString();
    }
    @PostMapping(value = "creditRequest", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String creditRequest(@RequestBody CreditRequest creditRequest) throws JsonProcessingException {
        log.debug("creditRequest: {}", creditRequest);
        return serviceRequestRouter.routeRequest(creditRequest.getClass().getName(), creditRequest, Boolean.FALSE, null).toString();
    }
    @PostMapping(value = "debitRequest", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String debitRequest(@RequestBody DebitRequest debitRequest) throws JsonProcessingException {
        log.debug("debitRequest: {}", debitRequest);
        return serviceRequestRouter.routeRequest(debitRequest.getClass().getName(), debitRequest, Boolean.FALSE, null).toString();
    }
    @PostMapping(value = "statementRequest", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String statementRequest(@RequestBody StatementRequest statementRequest) throws JsonProcessingException {
        log.debug("statementRequest: {}", statementRequest);
        return serviceRequestRouter.routeRequest(statementRequest.getClass().getName(), statementRequest, Boolean.FALSE, null).toString();
    }
    @PostMapping(value = "closeRequest", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String fundingRequest(@RequestBody CloseRequest closeRequest) throws JsonProcessingException {
        log.debug("closeRequest: {}", closeRequest);
        return serviceRequestRouter.routeRequest(closeRequest.getClass().getName(), closeRequest, Boolean.FALSE, null).toString();
    }
}