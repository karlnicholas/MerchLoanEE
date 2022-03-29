package com.github.karlnicholas.merchloan.servicerequest.repository;

import com.github.karlnicholas.merchloan.apimessage.message.ServiceRequestMessage;
import com.github.karlnicholas.merchloan.servicerequest.model.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, UUID> {
    List<ServiceRequest> findByStatus(ServiceRequestMessage.STATUS error);
    Boolean existsByStatusEquals(ServiceRequestMessage.STATUS status);
}
