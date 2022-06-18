package com.github.karlnicholas.merchloan.statement.dao;

import com.github.karlnicholas.merchloan.sqlutil.SqlUtils;
import com.github.karlnicholas.merchloan.statement.model.Statement;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
@Slf4j
public class StatementDao {
//    id, loan_id, statement_date, starting_balance, ending_balance, statement

    public void insert(Connection con, Statement statement) throws SQLException {
        con.setAutoCommit(false);
        try (PreparedStatement ps = con.prepareStatement("insert into statement(id, loan_id, statement_date, starting_balance, ending_balance, statement) values(?, ?, ?, ?, ?, ?)")) {
            ps.setBytes(1, SqlUtils.uuidToBytes(statement.getId()));
            ps.setBytes(2, SqlUtils.uuidToBytes(statement.getLoanId()));
            ps.setObject(3, statement.getStatementDate());
            ps.setBigDecimal(4, statement.getStartingBalance());
            ps.setBigDecimal(5, statement.getEndingBalance());
            ps.setString(6, statement.getStatementDoc());
            ps.executeUpdate();
        }
        con.commit();
    }

    public Optional<Statement> findById(Connection con, UUID id) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("select id, loan_id, statement_date, starting_balance, ending_balance, statement from statement where id = ?")) {
            ps.setBytes(1, SqlUtils.uuidToBytes(id));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(Statement.builder()
                            .id(SqlUtils.toUUID(rs.getBytes(1)))
                            .loanId(SqlUtils.toUUID(rs.getBytes(1)))
                            .statementDate(((Date) rs.getObject(3)).toLocalDate())
                            .startingBalance(rs.getBigDecimal(4))
                            .endingBalance(rs.getBigDecimal(5))
                            .statementDoc(rs.getString(6))
                            .build());
                else
                    return Optional.empty();
            }
        }
    }

    public List<Statement> findByLoanId(Connection con, UUID loanId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("select id, loan_id, statement_date, starting_balance, ending_balance, statement from statement where loan_id = ?")) {
            ps.setBytes(1, SqlUtils.uuidToBytes(loanId));
            try (ResultSet rs = ps.executeQuery()) {
                List<Statement> statements = new ArrayList<>();
                while (rs.next()) {
                    statements.add(Statement.builder()
                            .id(SqlUtils.toUUID(rs.getBytes(1)))
                            .loanId(SqlUtils.toUUID(rs.getBytes(1)))
                            .statementDate(((Date) rs.getObject(3)).toLocalDate())
                            .startingBalance(rs.getBigDecimal(4))
                            .endingBalance(rs.getBigDecimal(5))
                            .statementDoc(rs.getString(6))
                            .build());
                }
                return statements;
            }
        }
    }

    public Optional<Statement> findByLoanIdAndStatementDate(Connection con, UUID loanId, LocalDate statementDate) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("select id, loan_id, statement_date, starting_balance, ending_balance, statement from statement where loan_id = ? and statement_date = ?")) {
            ps.setBytes(1, SqlUtils.uuidToBytes(loanId));
            ps.setObject(2, statementDate);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(Statement.builder()
                            .id(SqlUtils.toUUID(rs.getBytes(1)))
                            .loanId(SqlUtils.toUUID(rs.getBytes(1)))
                            .statementDate(((Date) rs.getObject(3)).toLocalDate())
                            .startingBalance(rs.getBigDecimal(4))
                            .endingBalance(rs.getBigDecimal(5))
                            .statementDoc(rs.getString(6))
                            .build());
                else
                    return Optional.empty();
            }
        }
    }

    public Optional<Statement> findFirstByLoanIdOrderByStatementDateDesc(Connection con, UUID loanId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("select id, loan_id, statement_date, starting_balance, ending_balance, statement from statement where loan_id = ? order by statement_date desc limit 1")) {
            ps.setBytes(1, SqlUtils.uuidToBytes(loanId));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(Statement.builder()
                            .id(SqlUtils.toUUID(rs.getBytes(1)))
                            .loanId(SqlUtils.toUUID(rs.getBytes(1)))
                            .statementDate(((Date) rs.getObject(3)).toLocalDate())
                            .startingBalance(rs.getBigDecimal(4))
                            .endingBalance(rs.getBigDecimal(5))
                            .statementDoc(rs.getString(6))
                            .build());
                else
                    return Optional.empty();
            }
        }
    }
}
