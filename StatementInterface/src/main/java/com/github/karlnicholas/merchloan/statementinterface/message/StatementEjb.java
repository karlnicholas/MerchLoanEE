package com.github.karlnicholas.merchloan.statementinterface.message;


import com.github.karlnicholas.merchloan.jmsmessage.MostRecentStatement;

import javax.ejb.EJBException;
import javax.ejb.Remote;
import java.util.UUID;

@Remote
public interface StatementEjb {
    MostRecentStatement queryMostRecentStatement(UUID loanId) throws EJBException;
    String queryStatement(UUID loanId) throws EJBException;
    String queryStatements(UUID id) throws EJBException;
}