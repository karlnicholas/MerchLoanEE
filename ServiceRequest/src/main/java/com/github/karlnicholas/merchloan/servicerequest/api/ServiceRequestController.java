package com.github.karlnicholas.merchloan.servicerequest.api;

import com.github.karlnicholas.merchloan.apimessage.message.*;
import com.github.karlnicholas.merchloan.servicerequest.component.ServiceRequestRouter;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/api/v1/service/")
@Slf4j
public class ServiceRequestController {
    private final ServiceRequestRouter serviceRequestRouter;

    @Inject
    public ServiceRequestController(ServiceRequestRouter serviceRequestRouter) {

        this.serviceRequestRouter = serviceRequestRouter;
    }
    @POST
    @Path("accountRequest")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String createAccountRequest(AccountRequest accountRequest) throws Exception {
        log.debug("accountRequest: {}", accountRequest);
        return serviceRequestRouter.routeRequest(accountRequest.getClass().getName(), accountRequest, Boolean.FALSE, null).toString();
    }
    @POST
    @Path("fundingRequest")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String fundingRequest(FundingRequest fundingRequest) throws Exception {
        log.debug("fundingRequest: {}", fundingRequest);
        return serviceRequestRouter.routeRequest(fundingRequest.getClass().getName(), fundingRequest, Boolean.FALSE, null).toString();
    }
    @POST
    @Path("creditRequest")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String creditRequest(CreditRequest creditRequest) throws Exception {
        log.debug("creditRequest: {}", creditRequest);
        return serviceRequestRouter.routeRequest(creditRequest.getClass().getName(), creditRequest, Boolean.FALSE, null).toString();
    }
    @POST
    @Path("debitRequest")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String debitRequest(DebitRequest debitRequest) throws Exception {
        log.debug("debitRequest: {}", debitRequest);
        return serviceRequestRouter.routeRequest(debitRequest.getClass().getName(), debitRequest, Boolean.FALSE, null).toString();
    }
    @POST
    @Path("statementRequest")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String statementRequest(StatementRequest statementRequest) throws Exception {
        log.debug("statementRequest: {}", statementRequest);
        return serviceRequestRouter.routeRequest(statementRequest.getClass().getName(), statementRequest, Boolean.FALSE, null).toString();
    }
    @POST
    @Path("closeRequest")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String fundingRequest(CloseRequest closeRequest) throws Exception {
        log.debug("closeRequest: {}", closeRequest);
        return serviceRequestRouter.routeRequest(closeRequest.getClass().getName(), closeRequest, Boolean.FALSE, null).toString();
    }
}