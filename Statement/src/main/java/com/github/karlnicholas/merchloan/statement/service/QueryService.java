package com.github.karlnicholas.merchloan.statement.service;

import com.github.karlnicholas.merchloan.statement.dao.StatementDao;
import com.github.karlnicholas.merchloan.statement.model.Statement;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class QueryService {
    private final StatementDao statementDao;
    private final DataSource dataSource;

    public QueryService(StatementDao statementDao, DataSource dataSource) {
        this.statementDao = statementDao;
        this.dataSource = dataSource;
    }

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
