package com.github.karlnicholas.merchloan.accounts.dao;

import com.github.karlnicholas.merchloan.accounts.model.Account;
import com.github.karlnicholas.merchloan.sqlutil.UUIDToBytes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class AccountDao {
    public void createAccount(Connection con, Account account) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("insert into account values(?, ?, ?)")) {
            ps.setBytes(1, UUIDToBytes.uuidToBytes(account.getId()));
            ps.setString(2, account.getCustomer());
            ps.setObject(3, account.getCreateDate());
            ps.executeUpdate();
        }
    }

    public Optional<Account> findById(Connection con, UUID accountId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("select from account where id = ?")) {
            ps.setBytes(1, UUIDToBytes.uuidToBytes(accountId));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(Account.builder()
                            .id(UUIDToBytes.toUUID(rs.getBytes(1)))
                            .customer(rs.getString(2))
                            .createDate(((Date) rs.getObject(3)).toLocalDate())
                            .build());
                else
                    return Optional.empty();
            }
        }
    }
}
