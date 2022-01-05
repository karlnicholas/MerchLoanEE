package com.github.karlnicholas.merchloan.statement.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.karlnicholas.merchloan.jmsmessage.ServiceRequestResponse;
import com.github.karlnicholas.merchloan.statement.service.QueryService;
import com.github.karlnicholas.merchloan.statement.service.StatementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class RabbitMqReceiver implements RabbitListenerConfigurer {
    private final QueryService queryService;
    private final StatementService statementService;
    private final ObjectMapper objectMapper;

    public RabbitMqReceiver(QueryService queryService, StatementService statementService) {
        this.queryService = queryService;
        this.statementService = statementService;
        this.objectMapper = new ObjectMapper().findAndRegisterModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar rabbitListenerEndpointRegistrar) {
    }

    @RabbitListener(queues = "${rabbitmq.statement.statement.queue}", returnExceptions = "true")
    public void receivedServiceRequestMessage(ServiceRequestResponse serviceRequest) {
        try {
            log.info("ServiceRequestResponse Received {}", serviceRequest);
        } catch ( Exception ex) {
            log.error("void receivedServiceRequestMessage(ServiceRequestResponse serviceRequest) exception {}", ex.getMessage());
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }

    @RabbitListener(queues = "${rabbitmq.statement.query.queue}", returnExceptions = "true")
    public String receivedServiceRequestQueryIdMessage(UUID id) {
        try {
            log.info("ServiceRequestQueryId Received {}", id);
            return "hello";
        } catch ( Exception ex) {
            log.error("String receivedServiceRequestQueryIdMessage(UUID id) exception {}", ex.getMessage());
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }

}