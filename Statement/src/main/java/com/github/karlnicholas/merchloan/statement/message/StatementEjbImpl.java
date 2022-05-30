package com.github.karlnicholas.merchloan.statement.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.karlnicholas.merchloan.jmsmessage.MostRecentStatement;
import com.github.karlnicholas.merchloan.statement.model.Statement;
import com.github.karlnicholas.merchloan.statement.service.QueryService;
import com.github.karlnicholas.merchloan.statementinterface.message.StatementEjb;
import lombok.extern.slf4j.Slf4j;

import javax.ejb.*;
import javax.inject.Inject;
import java.util.UUID;

@Stateless
@Remote(StatementEjb.class)
@Slf4j
@TransactionManagement(TransactionManagementType.BEAN)
public class StatementEjbImpl implements StatementEjb {
    @Inject
    private QueryService queryService;
    private final ObjectMapper objectMapper;

    public StatementEjbImpl() {
        objectMapper = new ObjectMapper().findAndRegisterModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Override
    public MostRecentStatement queryMostRecentStatement(UUID loanId) throws EJBException {
        try {
            log.debug("onQueryMostRecentStatementMessage {}", loanId);
            MostRecentStatement mostRecentStatement = queryService.findMostRecentStatement(loanId)
                    .map(statement -> MostRecentStatement.builder()
                            .id(statement.getId())
                            .loanId(loanId)
                            .statementDate(statement.getStatementDate())
                            .endingBalance(statement.getEndingBalance())
                            .startingBalance(statement.getStartingBalance())
                            .build())
                    .orElse(MostRecentStatement.builder()
                            .loanId(loanId)
                            .build()
                    );
            return mostRecentStatement;
        } catch (Exception ex) {
            log.error("onQueryMostRecentStatementMessage exception", ex);
            throw new EJBException(ex);
        }
    }

    @Override
    public String queryStatement(UUID loanId) throws EJBException {
        try {
            log.debug("onQueryStatementMessage {}", loanId);
            String result = queryService.findById(loanId).map(Statement::getStatementDoc).orElse("ERROR: No statement found for id " + loanId);
            return result;
        } catch (Exception ex) {
            log.error("onQueryStatementMessage exception", ex);
            throw new EJBException(ex);
        }
    }

    @Override
    public String queryStatements(UUID id) throws EJBException {
        try {
            log.debug("onQueryStatementsMessage {}", id);
            return objectMapper.writeValueAsString(queryService.findByLoanId(id));
        } catch (Exception ex) {
            log.error("onQueryStatementsMessage exception", ex);
            throw new EJBException(ex);
        }
    }
}
