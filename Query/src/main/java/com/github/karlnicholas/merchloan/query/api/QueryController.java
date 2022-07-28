package com.github.karlnicholas.merchloan.query.api;

import com.github.karlnicholas.merchloan.query.service.QueryService;
import lombok.extern.slf4j.Slf4j;

import javax.ejb.EJBException;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

@Path("/query")
@Slf4j
public class QueryController {
    @Inject
    private QueryService queryService;

    @GET
    @Path("/request/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRequest(@PathParam("id") UUID id) throws JMSException, InterruptedException {
        log.debug("getRequest: {}", id);
        return queryService.getRequest(id);
    }
    @GET
    @Path("/account/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAccount(@PathParam("id") UUID id) throws JMSException, InterruptedException{
        log.debug("getAccount: {}", id);
        return queryService.getAccount(id);
    }
    @GET
    @Path("/loan/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLoan(@PathParam("id") UUID id) throws JMSException, InterruptedException {
        log.debug("getLoan: {}", id);
        return queryService.getLoan(id);
    }
    @GET
    @Path("statement/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatement(@PathParam("id") UUID id) throws JMSException, InterruptedException {
        log.debug("getStatement: {}", id);
        return queryService.getStatement(id);
    }
    @GET
    @Path("statements/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatements(@PathParam("id") UUID id) throws JMSException, InterruptedException {
        log.debug("getStatements: {}", id);
        return queryService.getStatements(id);
    }
    @GET
    @Path("checkrequests")
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkRequests() throws EJBException, JMSException, InterruptedException {
        log.debug("checkRequests");
        return queryService.getCheckRequests();
    }
}