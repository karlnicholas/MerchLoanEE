package com.github.karlnicholas.merchloan.statement.dao;

import com.github.karlnicholas.merchloan.sqlutil.UUIDToBytes;
import com.github.karlnicholas.merchloan.statement.model.Statement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class StatementDao {
//    id, loan_id, statement_date, starting_balance, ending_balance, statement

    public void insert(Connection con, Statement statement) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("insert into statement(id, loan_id, statement_date, starting_balance, ending_balance, status_message, statement) values(?, ?, ?, ?, ?, ?, ?)")) {
            ps.setBytes(1, UUIDToBytes.uuidToBytes(statement.getId()));
            ps.setBytes(2, UUIDToBytes.uuidToBytes(statement.getLoanId()));
            ps.setObject(3, statement.getStatementDate());
            ps.setBigDecimal(4, statement.getStartingBalance());
            ps.setBigDecimal(5, statement.getEndingBalance());
            ps.setBytes(6, statement.getStatement().getBytes(StandardCharsets.UTF_8));
            ps.executeUpdate();
        }
    }

    public Optional<Statement> findById(Connection con, UUID id) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("select id, loan_id, statement_date, starting_balance, ending_balance, statement from statement where id = ?")) {
            ps.setBytes(1, UUIDToBytes.uuidToBytes(id));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(Statement.builder()
                            .id(UUIDToBytes.toUUID(rs.getBytes(1)))
                            .loanId(UUIDToBytes.toUUID(rs.getBytes(1)))
                            .statementDate(((Date) rs.getObject(3)).toLocalDate())
                            .startingBalance(rs.getBigDecimal(4))
                            .endingBalance(rs.getBigDecimal(5))
                            .statement(new String(rs.getBytes(6), StandardCharsets.UTF_8))
                            .build());
                else
                    return Optional.empty();
            }
        }
    }

    public Iterator<Statement> findByLoanId(Connection con, UUID loanId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("select id, loan_id, statement_date, starting_balance, ending_balance, statement from statement where loan_id = ?")) {
            ps.setBytes(1, UUIDToBytes.uuidToBytes(loanId));
            try (ResultSet rs = ps.executeQuery()) {
                return new Iterator<Statement>() {
                    @Override
                    public boolean hasNext() {
                        try {
                            return rs.next();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public Statement next() {
                        try {
                            return Statement.builder()
                                    .id(UUIDToBytes.toUUID(rs.getBytes(1)))
                                    .loanId(UUIDToBytes.toUUID(rs.getBytes(1)))
                                    .statementDate(((Date) rs.getObject(3)).toLocalDate())
                                    .startingBalance(rs.getBigDecimal(4))
                                    .endingBalance(rs.getBigDecimal(5))
                                    .statement(new String(rs.getBytes(6), StandardCharsets.UTF_8))
                                    .build();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                };
            }
        }
    }

    public Optional<Statement> findByLoanIdAndStatementDate(Connection con, UUID loanId, LocalDate statementDate) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("select id, loan_id, statement_date, starting_balance, ending_balance, statement from statement where loan_id = ? and statement_date = ?")) {
            ps.setBytes(1, UUIDToBytes.uuidToBytes(loanId));
            ps.setObject(2, statementDate);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(Statement.builder()
                            .id(UUIDToBytes.toUUID(rs.getBytes(1)))
                            .loanId(UUIDToBytes.toUUID(rs.getBytes(1)))
                            .statementDate(((Date) rs.getObject(3)).toLocalDate())
                            .startingBalance(rs.getBigDecimal(4))
                            .endingBalance(rs.getBigDecimal(5))
                            .statement(new String(rs.getBytes(6), StandardCharsets.UTF_8))
                            .build());
                else
                    return Optional.empty();
            }
        }
    }

    public Optional<Statement> findFirstByLoanIdOrderByStatementDateDesc(Connection con, UUID loanId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("select id, loan_id, statement_date, starting_balance, ending_balance, statement from statement where loan_id = ? order by statement_date desc limit 1")) {
            ps.setBytes(1, UUIDToBytes.uuidToBytes(loanId));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(Statement.builder()
                            .id(UUIDToBytes.toUUID(rs.getBytes(1)))
                            .loanId(UUIDToBytes.toUUID(rs.getBytes(1)))
                            .statementDate(((Date) rs.getObject(3)).toLocalDate())
                            .startingBalance(rs.getBigDecimal(4))
                            .endingBalance(rs.getBigDecimal(5))
                            .statement(new String(rs.getBytes(6), StandardCharsets.UTF_8))
                            .build());
                else
                    return Optional.empty();
            }
        }
    }
}
