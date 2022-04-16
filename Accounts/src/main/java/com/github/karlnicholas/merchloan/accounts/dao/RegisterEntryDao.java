package com.github.karlnicholas.merchloan.accounts.dao;

import com.github.karlnicholas.merchloan.accounts.model.RegisterEntry;
import com.github.karlnicholas.merchloan.sqlutil.UUIDToBytes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
public class RegisterEntryDao {
    // id, loan_id, row_num, date, description, debit, credit
    public Optional<RegisterEntry> findById(Connection con, UUID loanId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("select id, loan_id, row_num, date, description, debit, credit from register_entry where id = ?")) {
            ps.setBytes(1, UUIDToBytes.uuidToBytes(loanId));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(RegisterEntry.builder()
                            .id(UUIDToBytes.toUUID(rs.getBytes(1)))
                            .loanId(UUIDToBytes.toUUID(rs.getBytes(2)))
                            .rowNum(rs.getInt(3))
                            .date(rs.getDate(4).toLocalDate())
                            .description(rs.getString(5))
                            .debit(rs.getBigDecimal(6))
                            .credit(rs.getBigDecimal(7))
                            .build());
                else
                    return Optional.empty();
            }
        }
    }
// id, credit, date, debit, description , loan_id , row_num
    public List<RegisterEntry> findByLoanIdAndDateBetweenOrderByRowNum(Connection con, UUID loanId, LocalDate startDate, LocalDate endDate) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("select id, loan_id, row_num, date, description, debit, credit from register_entry where loan_id = ? and date > ? and date <= ? order by row_num")) {
            ps.setBytes(1, UUIDToBytes.uuidToBytes(loanId));
            ps.setObject(2, startDate);
            ps.setObject(3, endDate);
            try (ResultSet rs = ps.executeQuery()) {
                List<RegisterEntry> registerEntries = new ArrayList<>();
                while (rs.next()) {
                    registerEntries.add(RegisterEntry.builder()
                            .id(UUIDToBytes.toUUID(rs.getBytes(1)))
                            .loanId(UUIDToBytes.toUUID(rs.getBytes(2)))
                            .rowNum(rs.getInt(3))
                            .date(rs.getDate(4).toLocalDate())
                            .description(rs.getString(5))
                            .debit(rs.getBigDecimal(6))
                            .credit(rs.getBigDecimal(7))
                            .build());
                }
                return registerEntries;
            }
        }
    }

    public List<RegisterEntry> findByLoanIdOrderByRowNum(Connection con, UUID loanId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("select id, loan_id, row_num, date, description, debit, credit from register_entry where loan_id = ? order by row_num")) {
            ps.setBytes(1, UUIDToBytes.uuidToBytes(loanId));
            try (ResultSet rs = ps.executeQuery()) {
                List<RegisterEntry> registerEntries = new ArrayList<>();
                while (rs.next()) {
                    registerEntries.add(RegisterEntry.builder()
                            .id(UUIDToBytes.toUUID(rs.getBytes(1)))
                            .loanId(UUIDToBytes.toUUID(rs.getBytes(2)))
                            .rowNum(rs.getInt(3))
                            .date(rs.getDate(4).toLocalDate())
                            .description(rs.getString(5))
                            .debit(rs.getBigDecimal(6))
                            .credit(rs.getBigDecimal(7))
                            .build());
                }
                return registerEntries;
            }
        }
    }

    public void insert(Connection con, RegisterEntry registerEntry) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("insert into register_entry(id, loan_id, row_num, date, description, debit, credit) values(?, ?, ?, ?, ?, ?, ?)")) {
            ps.setBytes(1, UUIDToBytes.uuidToBytes(registerEntry.getId()));
            ps.setBytes(2, UUIDToBytes.uuidToBytes(registerEntry.getLoanId()));
            ps.setInt(3, registerEntry.getRowNum());
            ps.setDate(4, java.sql.Date.valueOf(registerEntry.getDate()));
            ps.setString(5, registerEntry.getDescription());
            ps.setBigDecimal(6, registerEntry.getDebit());
            ps.setBigDecimal(7, registerEntry.getCredit());
            ps.executeUpdate();
        }
    }
}
