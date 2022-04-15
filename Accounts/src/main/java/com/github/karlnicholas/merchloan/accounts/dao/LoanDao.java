package com.github.karlnicholas.merchloan.accounts.dao;

import com.github.karlnicholas.merchloan.accounts.model.Account;
import com.github.karlnicholas.merchloan.accounts.model.Loan;
import com.github.karlnicholas.merchloan.sqlutil.UUIDToBytes;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

@Service
public class LoanDao {

    public void insert(Connection con, Loan loan) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("insert into loan values(?, ?, ?, ?, ?, ?, ?, ?, ?)" )) {
            ps.setBytes(1, UUIDToBytes.uuidToBytes(loan.getId()));
            ps.setBytes(2, UUIDToBytes.uuidToBytes(loan.getAccountId()));
            ps.setObject(3, loan.getStartDate());
            ps.setBytes(4, SerializationUtils.serialize(loan.getStatementDates()));
            ps.setBigDecimal(5, loan.getFunding());
            ps.setInt(6, loan.getMonths());
            ps.setBigDecimal(7, loan.getInterestRate());
            ps.setBigDecimal(8, loan.getMonthlyPayments());
            ps.setInt(9, loan.getLoanState().ordinal());
            ps.executeUpdate();
        }
    }

    public Optional<Loan> findById(Connection con, UUID loanId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("select from account where id = ?")) {
            ps.setBytes(1, UUIDToBytes.uuidToBytes(loanId));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(Loan.builder()
                            .id(UUIDToBytes.toUUID(rs.getBytes(1)))
                            .accountId(UUIDToBytes.toUUID(rs.getBytes(2)))
                            .startDate(((Date) rs.getObject(3)).toLocalDate())
                            .statementDates((List<LocalDate>) SerializationUtils.deserialize(rs.getBytes(4)))
                            .funding(rs.getBigDecimal(5))
                            .months(rs.getInt(6))
                            .interestRate(rs.getBigDecimal(7))
                            .monthlyPayments(rs.getBigDecimal(8))
                            .loanState(Loan.LOAN_STATE.values()[rs.getInt(9)])
                            .build());
                else
                    return Optional.empty();
            }
        }
    }

    public Iterator<Loan> findByLoanState(Connection con, Loan.LOAN_STATE state) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("select from loan where loan_state = ?")) {
            ps.setString(1, state.name());
            try (ResultSet rs = ps.executeQuery()) {
                return new Iterator() {
                    @Override
                    public boolean hasNext() {
                        try {
                            return rs.next();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    @Override
                    public Object next() {
                        try {
                            return Loan.builder()
                                    .id(UUIDToBytes.toUUID(rs.getBytes(1)))
                                    .accountId(UUIDToBytes.toUUID(rs.getBytes(2)))
                                    .startDate(((Date) rs.getObject(3)).toLocalDate())
                                    .statementDates((List<LocalDate>) SerializationUtils.deserialize(rs.getBytes(4)))
                                    .funding(rs.getBigDecimal(5))
                                    .months(rs.getInt(6))
                                    .interestRate(rs.getBigDecimal(7))
                                    .monthlyPayments(rs.getBigDecimal(8))
                                    .loanState(Loan.LOAN_STATE.values()[rs.getInt(9)])
                                    .build();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                };
            }
        }
    }

    public void updateState(Connection con, UUID loanId, Loan.LOAN_STATE state) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("update loan set loan_state = where id = ?" )) {
            ps.setString(1, state.name());
            ps.setBytes(2, UUIDToBytes.uuidToBytes(loanId));
            ps.executeUpdate();
        }
    }
}
