package com.github.karlnicholas.merchloan.servicerequest.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.karlnicholas.merchloan.jmsmessage.ServiceRequestResponse;
import com.github.karlnicholas.merchloan.servicerequest.model.ServiceRequest;
import com.github.karlnicholas.merchloan.servicerequest.service.QueryService;
import com.github.karlnicholas.merchloan.servicerequest.service.ServiceRequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class RabbitMqReceiver implements RabbitListenerConfigurer {
    private final QueryService queryService;
    private final ServiceRequestService serviceRequestService;
    private final ObjectMapper objectMapper;

    public RabbitMqReceiver(QueryService queryService, ServiceRequestService serviceRequestService, ObjectMapper objectMapper) {
        this.queryService = queryService;
        this.serviceRequestService = serviceRequestService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar rabbitListenerEndpointRegistrar) {
    }

    @RabbitListener(queues = "${rabbitmq.servicerequest.queue}", returnExceptions = "true")
    public void receivedServiceRequestMessage(ServiceRequestResponse serviceRequest) {
        try {
            log.info("ServiceRequestResponse Received {}", serviceRequest);
            serviceRequestService.completeServiceRequest(serviceRequest);
        } catch ( Exception ex) {
            log.error("void receivedServiceRequestMessage(ServiceRequestResponse serviceRequest) exception {}", ex.getMessage());
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }

    @RabbitListener(queues = "${rabbitmq.servicerequest.query.id.queue}", returnExceptions = "true")
    public String receivedServiceRequestQueryIdMessage(UUID id) {
        try {
            log.info("ServiceRequestQueryId Received {}", id);
            Optional<ServiceRequest> r = queryService.getServiceRequest(id);
            if ( r.isPresent() ) {
                return objectMapper.writeValueAsString(r.get());
            } else {
                return "ERROR: id not found: " + id;
            }
        } catch ( Exception ex) {
            log.error("String receivedServiceRequestQueryIdMessage(UUID id) exception {}", ex.getMessage());
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }

}