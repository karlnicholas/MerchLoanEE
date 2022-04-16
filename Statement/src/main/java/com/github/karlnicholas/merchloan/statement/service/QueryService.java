package com.github.karlnicholas.merchloan.statement.service;

import com.github.karlnicholas.merchloan.statement.dao.StatementDao;
import com.github.karlnicholas.merchloan.statement.model.Statement;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

@Service
public class QueryService {
    private final StatementDao statementDao;
    private final DataSource dataSource;

    public QueryService(StatementDao statementDao, DataSource dataSource) {
        this.statementDao = statementDao;
        this.dataSource = dataSource;
    }

    public Optional<Statement> findById(UUID id) {
        try (Connection con = dataSource.getConnection()) {
            return statementDao.findById(con, id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Statement> findByLoanId(UUID loanId) {
        try (Connection con = dataSource.getConnection()) {
            return statementDao.findByLoanId(con, loanId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Statement> findMostRecentStatement(UUID loanId) throws SQLException {
        try (Connection con = dataSource.getConnection()) {
            return statementDao.findFirstByLoanIdOrderByStatementDateDesc(con, loanId);
        }
    }
}
