package com.github.karlnicholas.merchloan.query.api;

import com.github.karlnicholas.merchloan.query.message.MQProducers;

import javax.ejb.EJBException;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

@ApplicationScoped
public class ApiHandler {
    @Inject
    private MQProducers mqProducers;

    public Response getRequest(UUID id) throws EJBException {
        return Response.ok(mqProducers.queryServiceRequest(id), MediaType.APPLICATION_JSON).build();
    }

//    public Response getAccount(UUID id) {
//        return Response.ok(mqProducers.queryAccount(id), MediaType.APPLICATION_JSON).build();
//    }
//
//    public Response getLoan(UUID id) {
//        return Response.ok(mqProducers.queryLoan(id), MediaType.APPLICATION_JSON).build();
//    }
//
//    public Response getStatement(UUID id) {
//        return Response.ok(mqProducers.queryStatement(id), MediaType.APPLICATION_JSON).build();
//    }
//
//    public Response getStatements(UUID id) {
//        return Response.ok(mqProducers.queryStatements(id), MediaType.APPLICATION_JSON).build();
//    }

    public Response getCheckRequests() throws Throwable {
        return Response.ok(mqProducers.queryCheckRequest(), MediaType.APPLICATION_JSON).build();
    }
}
