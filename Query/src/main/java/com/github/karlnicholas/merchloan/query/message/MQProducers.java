package com.github.karlnicholas.merchloan.query.message;

import com.github.karlnicholas.merchloan.accountsinterface.message.AccountsEjb;
import com.github.karlnicholas.merchloan.servicerequestinterface.message.ServiceRequestEjb;
import com.github.karlnicholas.merchloan.statementinterface.message.StatementEjb;
import lombok.extern.slf4j.Slf4j;

import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
@Slf4j
public class MQProducers {
    @EJB(lookup = "ejb:merchloanee/servicerequest/ServiceRequestEjbImpl!com.github.karlnicholas.merchloan.servicerequestinterface.message.ServiceRequestEjb")
    private ServiceRequestEjb serviceRequestEjb;
    @EJB(lookup = "ejb:merchloanee/accounts/AccountsEjbImpl!com.github.karlnicholas.merchloan.accountsinterface.message.AccountsEjb")
    private AccountsEjb accountsEjb;
    @EJB(lookup = "ejb:merchloanee/statement/StatementEjbImpl!com.github.karlnicholas.merchloan.statementinterface.message.StatementEjb")
    private StatementEjb statementEjb;

    public Object queryServiceRequest(UUID id) {
        return serviceRequestEjb.queryId(id);
    }

    public Object queryCheckRequest() {
        return serviceRequestEjb.checkRequest();
    }

    public Object queryAccount(UUID id){
        return accountsEjb.queryAccountId(id);
    }

    public Object queryLoan(UUID id) {
        return accountsEjb.queryLoanId(id);
    }

    public Object queryStatement(UUID id) {
        return statementEjb.queryStatement(id);
    }

    public Object queryStatements(UUID id) {
        return statementEjb.queryStatements(id);
    }

}
