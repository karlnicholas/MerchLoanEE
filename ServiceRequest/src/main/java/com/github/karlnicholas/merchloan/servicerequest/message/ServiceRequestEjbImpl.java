package com.github.karlnicholas.merchloan.servicerequest.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.karlnicholas.merchloan.dto.RequestStatusDto;
import com.github.karlnicholas.merchloan.servicerequest.model.ServiceRequest;
import com.github.karlnicholas.merchloan.servicerequest.service.QueryService;
import com.github.karlnicholas.merchloan.servicerequestinterface.message.ServiceRequestEjb;
import lombok.extern.slf4j.Slf4j;

import javax.ejb.EJBException;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

@Stateless
@Remote(ServiceRequestEjb.class)
@Slf4j
public class ServiceRequestEjbImpl implements ServiceRequestEjb {

    private final QueryService queryService;
    private final ObjectMapper objectMapper;

    @Inject
    public ServiceRequestEjbImpl(QueryService queryService) {
        this.queryService = queryService;
        this.objectMapper = new ObjectMapper().findAndRegisterModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Override
    public String queryId(UUID id) throws EJBException {
        try {
            log.debug("receivedServiceRequestQueryIdMessage: {}", id);
            Optional<ServiceRequest> requestOpt = queryService.getServiceRequest(id);
            String response;
            if (requestOpt.isPresent()) {
                ServiceRequest request = requestOpt.get();
                response = objectMapper.writeValueAsString(RequestStatusDto.builder()
                        .id(request.getId())
                        .localDateTime(request.getLocalDateTime())
                        .status(request.getStatus().name())
                        .statusMessage(request.getStatusMessage())
                        .build());
            } else {
                response = "ERROR: id not found: " + id;
            }
            return response;
        } catch (Exception e) {
            log.error("receivedCheckRequestMessage", e);
            throw new EJBException(e);
        }
    }

    @Override
    public Boolean checkRequest() throws EJBException {
        log.trace("receivedCheckRequestMessage");
        try {
            return queryService.checkRequest();
        } catch (SQLException e) {
            log.error("receivedCheckRequestMessage", e);
            throw new EJBException(e);
        }
    }
}
