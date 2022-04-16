package com.github.karlnicholas.merchloan.accounts.dao;

import com.github.karlnicholas.merchloan.accounts.model.LoanState;
import com.github.karlnicholas.merchloan.sqlutil.UUIDToBytes;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.Optional;
import java.util.UUID;

@Service
public class LoanStateDao {
    //     loan_id, start_date, current_row_num, balance
    public void insert(Connection con, LoanState loanState) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("insert into loan_state(loan_id, start_date, balance, current_row_num) values(?, ?, ?, ?)")) {
            ps.setBytes(1, UUIDToBytes.uuidToBytes(loanState.getLoanId()));
            ps.setDate(2, java.sql.Date.valueOf(loanState.getStartDate()));
            ps.setBigDecimal(3, loanState.getBalance());
            ps.setInt(4, loanState.getCurrentRowNum());
            ps.executeUpdate();
        }
    }

    public void updateWithCommit(Connection con, LoanState loanState) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("update loan_state set start_date = ?, balance = ?, current_row_num = ? where loan_id = ?")) {
            ps.setDate(1, java.sql.Date.valueOf(loanState.getStartDate()));
            ps.setBigDecimal(2, loanState.getBalance());
            ps.setInt(3, loanState.getCurrentRowNum());
            ps.setBytes(4, UUIDToBytes.uuidToBytes(loanState.getLoanId()));
            ps.executeUpdate();
        }
        try( Statement s = con.createStatement() ) {
            s.execute("commit");
        }
    }

    public Optional<LoanState> findByLoanIdForUpdate(Connection con, UUID loanId) throws SQLException {
        try( Statement s = con.createStatement() ) {
            s.execute("begin");
        }
        try (PreparedStatement ps = con.prepareStatement("select loan_id, start_date, balance, current_row_num from loan_state where loan_id = ? for update;")) {
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

}
