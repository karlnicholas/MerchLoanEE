package com.github.karlnicholas.merchloan.servicerequest.service;

import com.github.karlnicholas.merchloan.jmsmessage.ServiceRequestResponse;
import com.github.karlnicholas.merchloan.servicerequest.model.ServiceRequest;
import com.github.karlnicholas.merchloan.servicerequest.repository.ServiceRequestRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
public class QueryService {
    private final ServiceRequestRepository serviceRequestRepository;

    public QueryService(ServiceRequestRepository serviceRequestRepository) {
        this.serviceRequestRepository = serviceRequestRepository;
    }

    public Optional<ServiceRequest> getServiceRequest(UUID id) {
        return serviceRequestRepository.findById(id);
    }

    public Boolean checkRequest(LocalDate businessDate) {
        return serviceRequestRepository.existsBylocalDateTimeLessThanAndStatusEquals(businessDate.plusDays(1).atStartOfDay(), ServiceRequestResponse.STATUS.PENDING);
    }
}
