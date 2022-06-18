package com.github.karlnicholas.merchloan.businessdate.businessdate.dao;

import com.github.karlnicholas.merchloan.businessdate.businessdate.model.BusinessDate;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@ApplicationScoped
@Slf4j
public class BusinessDateDao {
    public Optional<BusinessDate> findById(Connection con, Long businessDateId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("select id, business_date from business_date where id = ?")) {
            ps.setLong(1, businessDateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(BusinessDate.builder()
                            .id(rs.getLong(1))
                            .date(rs.getDate(2).toLocalDate())
                            .build());
                else
                    return Optional.empty();
            }
        }
    }

    public void updateDate(Connection con, BusinessDate businessDate) throws SQLException {
        con.setAutoCommit(false);
        try (PreparedStatement ps = con.prepareStatement("update business_date set business_date = ? where id = ?")) {
            ps.setDate(1, java.sql.Date.valueOf(businessDate.getDate()));
            ps.setLong(2, businessDate.getId());
            ps.executeUpdate();
        }
        con.commit();
    }

    public void insert(Connection con, BusinessDate businessDate) throws SQLException {
        con.setAutoCommit(false);
        try (PreparedStatement ps = con.prepareStatement("insert into business_date(id, business_date) values(?, ?)")) {
            ps.setLong(1, businessDate.getId());
            ps.setDate(2, java.sql.Date.valueOf(businessDate.getDate()));
            ps.executeUpdate();
        }
        con.commit();
    }
}
