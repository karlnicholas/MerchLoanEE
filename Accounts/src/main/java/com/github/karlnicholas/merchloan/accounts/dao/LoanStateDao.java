package com.github.karlnicholas.merchloan.accounts.dao;

import com.github.karlnicholas.merchloan.accounts.model.Loan;
import com.github.karlnicholas.merchloan.accounts.model.LoanState;
import com.github.karlnicholas.merchloan.sqlutil.UUIDToBytes;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import java.sql.*;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LoanStateDao {
    public void insert(Connection con, LoanState loanState) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("insert into loan_state values(?, ?, ?, ?)")) {
            ps.setBytes(1, UUIDToBytes.uuidToBytes(loanState.getLoanId()));
            ps.setObject(2, loanState.getStartDate());
            ps.setBigDecimal(3, loanState.getBalance());
            ps.setInt(4, loanState.getCurrentRowNum());
            ps.executeUpdate();
        }
    }

    public void update(Connection con, LoanState loanState) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("insert into loan_state values(?, ?, ?) where loanId = ?")) {
            ps.setObject(1, loanState.getStartDate());
            ps.setBigDecimal(2, loanState.getBalance());
            ps.setInt(3, loanState.getCurrentRowNum());
            ps.setBytes(4, UUIDToBytes.uuidToBytes(loanState.getLoanId()));
            ps.executeUpdate();
        }
    }


    public Optional<LoanState> getWithWriteLock(Connection con, UUID loanId) {
        try (PreparedStatement ps = con.prepareStatement("select from loan_state where loanId = ?")) {
            ps.setObject(1, loanState.getStartDate());
            ps.setBigDecimal(2, loanState.getBalance());
            ps.setInt(3, loanState.getCurrentRowNum());
            ps.setBytes(4, UUIDToBytes.uuidToBytes(loanState.getLoanId()));
            ps.executeUpdate();
        }
    }
}
