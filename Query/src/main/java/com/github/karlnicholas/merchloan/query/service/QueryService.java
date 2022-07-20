package com.github.karlnicholas.merchloan.query.service;

import com.github.karlnicholas.merchloan.accountsinterface.message.AccountsEjb;
import com.github.karlnicholas.merchloan.servicerequestinterface.message.ServiceRequestEjb;
import com.github.karlnicholas.merchloan.statementinterface.message.StatementEjb;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

@ApplicationScoped
public class QueryService {
    @EJB(lookup = "ejb:merchloanee/servicerequest/ServiceRequestEjbImpl!com.github.karlnicholas.merchloan.servicerequestinterface.message.ServiceRequestEjb")
    private ServiceRequestEjb serviceRequestEjb;
    @EJB(lookup = "ejb:merchloanee/accounts/AccountsEjbImpl!com.github.karlnicholas.merchloan.accountsinterface.message.AccountsEjb")
    private AccountsEjb accountsEjb;
    @EJB(lookup = "ejb:merchloanee/statement/StatementEjbImpl!com.github.karlnicholas.merchloan.statementinterface.message.StatementEjb")
    private StatementEjb statementEjb;

    public Response getRequest(UUID id) throws EJBException {
        return Response.ok(serviceRequestEjb.queryId(id), MediaType.APPLICATION_JSON).build();
    }

    public Response getCheckRequests() throws EJBException {
        return Response.ok(serviceRequestEjb.checkRequest(), MediaType.APPLICATION_JSON).build();
    }

    public Response getAccount(UUID id) {
        return Response.ok(accountsEjb.queryAccountId(id), MediaType.APPLICATION_JSON).build();
    }

    public Response getLoan(UUID id) {
        return Response.ok(accountsEjb.queryLoanId(id), MediaType.APPLICATION_JSON).build();
    }

    public Response getStatement(UUID id) {
        return Response.ok(statementEjb.queryStatement(id), MediaType.APPLICATION_JSON).build();
    }

    public Response getStatements(UUID id) {
        return Response.ok(statementEjb.queryStatements(id), MediaType.APPLICATION_JSON).build();
    }
}
