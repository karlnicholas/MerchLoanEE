package com.github.karlnicholas.merchloan.servicerequest.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.karlnicholas.merchloan.apimessage.message.StatementRequest;
import com.github.karlnicholas.merchloan.dto.RequestStatusDto;
import com.github.karlnicholas.merchloan.jms.message.RabbitMqSender;
import com.github.karlnicholas.merchloan.jmsmessage.*;
import com.github.karlnicholas.merchloan.servicerequest.model.ServiceRequest;
import com.github.karlnicholas.merchloan.servicerequest.service.QueryService;
import com.github.karlnicholas.merchloan.servicerequest.service.ServiceRequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
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
        // no configuration needed
    }

    @RabbitListener(queues = "${rabbitmq.servicerequest.queue}", returnExceptions = "true")
    public void receivedServiceRequestMessage(ServiceRequestResponse serviceRequest) {
        try {
            log.info("ServiceRequestResponse Received {}", serviceRequest);
            serviceRequestService.completeServiceRequest(serviceRequest);
        } catch (Exception ex) {
            log.error("void receivedServiceRequestMessage exception {}", ex.getMessage());
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }

    @RabbitListener(queues = "${rabbitmq.servicerequest.billloan.queue}", returnExceptions = "true")
    public void receivedServiceRequestBillloanMessage(BillingCycle billingCycle) {
        try {
            log.info("Billloan Received {}", billingCycle);
            serviceRequestService.statementStatementRequest(StatementRequest.builder()
                            .loanId(billingCycle.getLoanId())
                            .statementDate(billingCycle.getStatementDate())
                            .startDate(billingCycle.getStartDate())
                            .endDate(billingCycle.getEndDate())
                            .build(),
                    Boolean.FALSE, null);
        } catch (Exception ex) {
            log.error("String receivedServiceRequestBillloanMessage exception {}", ex.getMessage());
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }

    @RabbitListener(queues = "${rabbitmq.servicerequest.statementcomplete.queue}", returnExceptions = "true")
    public void receivedServiceStatementCompleteMessage(StatementCompleteResponse statementCompleteResponse) {
        try {
            log.info("StatementComplete Received {}", statementCompleteResponse);
            serviceRequestService.statementComplete(statementCompleteResponse);
        } catch (Exception ex) {
            log.error("String receivedServiceStatementCompleteMessage exception {}", ex.getMessage());
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }

    @RabbitListener(queues = "${rabbitmq.servicerequest.query.id.queue}", returnExceptions = "true")
    public String receivedServiceRequestQueryIdMessage(UUID id) {
        try {
            log.info("ServiceRequestQueryId Received {}", id);
            Optional<ServiceRequest> requestOpt = queryService.getServiceRequest(id);
            if (requestOpt.isPresent()) {
                ServiceRequest request = requestOpt.get();
                return objectMapper.writeValueAsString(RequestStatusDto.builder()
                        .id(request.getId())
                        .localDateTime(request.getLocalDateTime())
                        .status(request.getStatus().name())
                        .statusMessage(request.getStatusMessage())
                        .build()
                );
            } else {
                return "ERROR: id not found: " + id;
            }
        } catch (Exception ex) {
            log.error("String receivedServiceRequestQueryIdMessage exception {}", ex.getMessage());
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }

    @RabbitListener(queues = "${rabbitmq.servicerequest.checkrequest.queue}")
    public Boolean receivedCheckRequestMessage() {
        try {
            log.info("CheckRequest Received");
            return queryService.checkRequest();
        } catch (Exception ex) {
            log.error("String receivedCheckRequestMessage exception {}", ex.getMessage());
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }

}