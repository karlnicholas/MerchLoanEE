package com.github.karlnicholas.merchloan.servicerequest.dao;

import com.github.karlnicholas.merchloan.apimessage.message.ServiceRequestMessage;
import com.github.karlnicholas.merchloan.servicerequest.model.ServiceRequest;
import com.github.karlnicholas.merchloan.sqlutil.UUIDToBytes;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class ServiceRequestDao {
    public Optional<ServiceRequest> findById(Connection con, UUID id) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("select id, request, request_type, local_date_time, status, status_message, retry_count from service_request where id = ?")) {
            ps.setBytes(1, UUIDToBytes.uuidToBytes(id));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(ServiceRequest.builder()
                            .id(UUIDToBytes.toUUID(rs.getBytes(1)))
                            .request(rs.getString(2))
                            .requestType(rs.getString(3))
                            .localDateTime(LocalDateTime.parse(rs.getString(4)))
                            .status(ServiceRequestMessage.STATUS.values()[rs.getInt(5)])
                            .statusMessage(rs.getString(6))
                            .retryCount(rs.getInt(7))
                            .build());
                else
                    return Optional.empty();
            }
        }
    }
//    private UUID id;
//    private String request;
//    private String requestType;
//    private LocalDateTime localDateTime;
//    private ServiceRequestMessage.STATUS status;
//    private String statusMessage;
//    private Integer retryCount;

    public Boolean existsByStatusEquals(Connection con, ServiceRequestMessage.STATUS status) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("select count(*) as count from service_request where status = ?")) {
            ps.setInt(1, status.ordinal());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1) > 0;
                else
                    return Boolean.FALSE;
            }
        }
    }

    public Iterator<ServiceRequest> findByStatus(Connection con, ServiceRequestMessage.STATUS status) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("select id, request, request_type, local_date_time, status, status_message, retry_count from service_request where status = ?")) {
            ps.setInt(1, status.ordinal());
            try (ResultSet rs = ps.executeQuery()) {
                return new Iterator<ServiceRequest>() {
                    @Override
                    public boolean hasNext() {
                        try {
                            return rs.next();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public ServiceRequest next() {
                        try {
                            return ServiceRequest.builder()
                                    .id(UUIDToBytes.toUUID(rs.getBytes(1)))
                                    .request(rs.getString(2))
                                    .requestType(rs.getString(3))
                                    .localDateTime(LocalDateTime.parse(rs.getString(4)))
                                    .status(ServiceRequestMessage.STATUS.values()[rs.getInt(5)])
                                    .statusMessage(rs.getString(6))
                                    .retryCount(rs.getInt(7))
                                    .build();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                };
            }
        }
    }

    public void update(Connection con, ServiceRequest serviceRequest) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("update service_request set status = ?, status_message = ?, retry_count = ? where id = ?")) {
            ps.setInt(1, serviceRequest.getStatus().ordinal());
            ps.setString(2, serviceRequest.getStatusMessage());
            ps.setInt(3, serviceRequest.getRetryCount());
            ps.setBytes(4, UUIDToBytes.uuidToBytes(serviceRequest.getId()));
            ps.executeUpdate();
        }
    }

    public void insert(Connection con, ServiceRequest serviceRequest) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("insert into service_request(id, request, request_type, local_date_time, status, status_message, retry_count) values(?, ?, ?, ?, ?, ?, ?)")) {
            ps.setBytes(1, UUIDToBytes.uuidToBytes(serviceRequest.getId()));
            ps.setString(2, serviceRequest.getRequest());
            ps.setString(3, serviceRequest.getRequestType());
            ps.setObject(4, serviceRequest.getLocalDateTime());
            ps.setInt(5, serviceRequest.getStatus().ordinal());
            ps.setString(6, serviceRequest.getStatusMessage());
            ps.setInt(7, serviceRequest.getRetryCount());
            ps.executeUpdate();
        }
    }

}
