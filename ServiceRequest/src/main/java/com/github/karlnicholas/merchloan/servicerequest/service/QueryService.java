package com.github.karlnicholas.merchloan.servicerequest.service;

import com.github.karlnicholas.merchloan.servicerequest.dao.ServiceRequestDao;
import com.github.karlnicholas.merchloan.servicerequest.model.ServiceRequest;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
@Slf4j
public class QueryService {
    private final ServiceRequestDao serviceRequestDao;
    @Resource(lookup = "java:jboss/datasources/ServiceRequestDS")
    private DataSource dataSource;

    @Inject
    public QueryService(ServiceRequestDao serviceRequestDao) {
        this.serviceRequestDao = serviceRequestDao;
    }

    public Optional<ServiceRequest> getServiceRequest(UUID id) throws SQLException {
        try (Connection con = dataSource.getConnection()) {
            return serviceRequestDao.findById(con, id);
        }
    }

    public Boolean checkRequest() throws SQLException {
        try (Connection con = dataSource.getConnection()) {
            return serviceRequestDao.existsStillProcessing(con);
        }
    }
}
