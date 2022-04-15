package com.github.karlnicholas.merchloan.businessdate.businessdate.dao;

import com.github.karlnicholas.merchloan.businessdate.businessdate.model.BusinessDate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.Optional;

@Service
@Slf4j
public class BusinessDateDao {
    public Optional<BusinessDate> findById(Connection con, Long businessDateId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("select id, business_date from account where id = ?")) {
            ps.setLong(1, businessDateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(BusinessDate.builder()
                            .id(rs.getLong(1))
                            .businessDate(rs.getDate(2).toLocalDate())
                            .build());
                else
                    return Optional.empty();
            }
        }
    }

    public void updateDate(Connection con, BusinessDate businessDate) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("update business_date set business_date = where id = ?")) {
            ps.setDate(1, java.sql.Date.valueOf(businessDate.getBusinessDate()));
            ps.setLong(2, businessDate.getId());
            ps.executeUpdate();
        }
    }
}
