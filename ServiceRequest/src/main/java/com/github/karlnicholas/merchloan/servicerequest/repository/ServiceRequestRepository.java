package com.github.karlnicholas.merchloan.servicerequest.repository;

import com.github.karlnicholas.merchloan.servicerequest.model.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, UUID> {
}
