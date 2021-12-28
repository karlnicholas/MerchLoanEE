package com.github.karlnicholas.merchloan.servicerequest.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.karlnicholas.merchloan.servicerequest.model.ServiceRequest;
import com.github.karlnicholas.merchloan.servicerequest.service.QueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class RabbitMqReceiver implements RabbitListenerConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(RabbitMqReceiver.class);
    private final QueryService queryService;
    private final ObjectMapper objectMapper;

    public RabbitMqReceiver(QueryService queryService, ObjectMapper objectMapper) {
        this.queryService = queryService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar rabbitListenerEndpointRegistrar) {
    }

    @RabbitListener(queues = "${rabbitmq.servicerequest.query.id.queue}", returnExceptions = "true")
    public String receivedAccountMessage(UUID id) throws JsonProcessingException {
        logger.info("CreateAccount Details Received is.. " + id);
        Optional<ServiceRequest> r = queryService.getServiceRequest(id);
        if ( r.isPresent() ) {
            return objectMapper.writeValueAsString(r.get());
        } else {
            throw new IllegalArgumentException(id + " not found");
        }
    }

}