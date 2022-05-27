package com.github.karlnicholas.merchloan.statement.service;

import com.github.karlnicholas.merchloan.statement.dao.StatementDao;
import com.github.karlnicholas.merchloan.statement.model.Statement;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class QueryService {
    @Inject
    private StatementDao statementDao;
    @Resource(lookup = "java:jboss/datasources/StatementDS")
    private DataSource dataSource;

    public Optional<Statement> findById(UUID id) throws SQLException {
        try (Connection con = dataSource.getConnection()) {
            return statementDao.findById(con, id);
        }
    }

    public List<Statement> findByLoanId(UUID loanId) throws SQLException {
        try (Connection con = dataSource.getConnection()) {
            return statementDao.findByLoanId(con, loanId);
        }
    }

    public Optional<Statement> findMostRecentStatement(UUID loanId) throws SQLException {
        try (Connection con = dataSource.getConnection()) {
            return statementDao.findFirstByLoanIdOrderByStatementDateDesc(con, loanId);
        }
    }
}
