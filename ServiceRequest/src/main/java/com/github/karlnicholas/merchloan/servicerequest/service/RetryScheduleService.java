package com.github.karlnicholas.merchloan.servicerequest.service;

import com.github.karlnicholas.merchloan.apimessage.message.ServiceRequestMessage;
import com.github.karlnicholas.merchloan.servicerequest.dao.ServiceRequestDao;
import com.github.karlnicholas.merchloan.servicerequest.model.ServiceRequest;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@ApplicationScoped
public class RetryScheduleService {
    private final ServiceRequestDao serviceRequestDao;
    private final RetryService retryService;
    @Resource(lookup = "java:jboss/datasources/ServiceRequestDS")
    private DataSource dataSource;

    @Inject
    public RetryScheduleService(ServiceRequestDao serviceRequestDao, RetryService retryService) {
        this.serviceRequestDao = serviceRequestDao;
        this.retryService = retryService;
    }

    public void retryService() throws SQLException {
        try (Connection con = dataSource.getConnection()) {
            List<ServiceRequest> serviceRequests = serviceRequestDao.findByStatus(con, ServiceRequestMessage.STATUS.ERROR);
            serviceRequests.forEach(sr->{
                sr.setRetryCount(sr.getRetryCount() + 1);
                if (sr.getRetryCount() > 3) {
                    sr.setStatus(ServiceRequestMessage.STATUS.FAILURE);
                    sr.setStatusMessage("Exceeded Retry Count");
                }
                try {
                    serviceRequestDao.update(con, sr);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                if (sr.getRetryCount() <= 3) {
                    retryService.retryServiceRequest(sr, sr.getRequestType());
                }
            });
        }
    }
}
