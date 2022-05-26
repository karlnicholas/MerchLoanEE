package com.github.karlnicholas.merchloan.query.api;

import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.UUID;

@Path("/api/query")
@Slf4j
public class QueryController {
    private final ApiHandler apiHandler;

    @Inject
    public QueryController(ApiHandler apiHandler) {
        this.apiHandler = apiHandler;
    }
    @GET
    @Path("/request/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getRequest(@PathParam("id") UUID id) throws Throwable {
        log.debug("getRequest: {}", id);
        return apiHandler.getRequest(id).readEntity(String.class);
    }
//    @GET
//    @Path("/account/{id}")
//    @Produces(MediaType.APPLICATION_JSON)
//    public String getAccount(@PathParam("id") UUID id) {
//        log.debug("getAccount: {}", id);
//        return apiHandler.getAccount(id).readEntity(String.class);
//    }
//    @GET
//    @Path("/loan/{id}")
//    @Produces(MediaType.APPLICATION_JSON)
//    public String getLoan(@PathParam("id") UUID id) {
//        log.debug("getLoan: {}", id);
//        return apiHandler.getLoan(id).readEntity(String.class);
//    }
//    @GET
//    @Path("statement/{id}")
//    @Produces(MediaType.APPLICATION_JSON)
//    public String getStatement(@PathParam("id") UUID id) {
//        log.debug("getStatement: {}", id);
//        return apiHandler.getStatement(id).readEntity(String.class);
//    }
//    @GET
//    @Path("statements/{id}")
//    @Produces(MediaType.APPLICATION_JSON)
//    public String getStatements(@PathParam("id") UUID id) {
//        log.debug("getStatements: {}", id);
//        return apiHandler.getStatements(id).readEntity(String.class);
//    }
    @GET
    @Path("checkrequests")
    @Produces(MediaType.APPLICATION_JSON)
    public String checkRequests() throws Throwable {
        log.debug("checkRequests");
        return apiHandler.getCheckRequests().readEntity(Boolean.class).toString();
    }
}