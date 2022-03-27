package com.github.karlnicholas.merchloan.servicerequest.service;

import com.github.karlnicholas.merchloan.apimessage.message.ServiceRequestMessage;
import com.github.karlnicholas.merchloan.servicerequest.model.ServiceRequest;
import com.github.karlnicholas.merchloan.servicerequest.repository.ServiceRequestRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class QueryService {
    private final ServiceRequestRepository serviceRequestRepository;

    public QueryService(ServiceRequestRepository serviceRequestRepository) {
        this.serviceRequestRepository = serviceRequestRepository;
    }

    public Optional<ServiceRequest> getServiceRequest(UUID id) {
        return serviceRequestRepository.findById(id);
    }

    public Boolean checkRequest() {
        log.debug("checkRequest {}", serviceRequestRepository.findAll().stream().map(sr->System.lineSeparator() + "\t" + sr.getLocalDateTime() + ":" + sr.getStatus() + ":" + sr.getRequestType()).collect(Collectors.toList()));
        return serviceRequestRepository.existsByStatusEquals(ServiceRequestMessage.STATUS.PENDING);
    }
}
