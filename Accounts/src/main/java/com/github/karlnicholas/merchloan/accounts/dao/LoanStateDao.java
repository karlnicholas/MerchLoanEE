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
    //     loan_id, start_date, current_row_num, balance
    public void insert(Connection con, LoanState loanState) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("insert into loan_state(loan_id, start_date, balance, row_num) values(?, ?, ?, ?)")) {
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

    public Optional<LoanState> findByLoanId(Connection con, UUID loanId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("select loan_id, start_date, balance, row_num from loan_state where loanId = ?")) {
            ps.setBytes(1, UUIDToBytes.uuidToBytes(loanId));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.ofNullable(LoanState.builder()
                            .loanId((UUIDToBytes.toUUID(rs.getBytes(1))))
                            .startDate(((Date) rs.getObject(2)).toLocalDate())
                            .balance(rs.getBigDecimal(3))
                            .currentRowNum(rs.getInt(4))
                            .build());
                else
                    return Optional.empty();
            }
        }
    }

    public void readWriteTablesLock(Connection con) throws SQLException {
        try (Statement s = con.createStatement()) {
            s.execute("lock tables loan_state write, loan_state read;");
        }
    }

    public void tablesUnlock(Connection con) throws SQLException {
        try (Statement s = con.createStatement()) {
            s.execute("unlock tables;");
        }
    }

}
