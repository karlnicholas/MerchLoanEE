package com.github.karlnicholas.merchloan.servicerequest.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.karlnicholas.merchloan.apimessage.message.StatementRequest;
import com.github.karlnicholas.merchloan.dto.RequestStatusDto;
import com.github.karlnicholas.merchloan.jms.MQConsumerUtils;
import com.github.karlnicholas.merchloan.jmsmessage.BillingCycle;
import com.github.karlnicholas.merchloan.jmsmessage.ServiceRequestResponse;
import com.github.karlnicholas.merchloan.jmsmessage.StatementCompleteResponse;
import com.github.karlnicholas.merchloan.servicerequest.component.ServiceRequestException;
import com.github.karlnicholas.merchloan.servicerequest.model.ServiceRequest;
import com.github.karlnicholas.merchloan.servicerequest.service.QueryService;
import com.github.karlnicholas.merchloan.servicerequest.service.ServiceRequestService;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Delivery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class MQConsumers {
    private final ServiceRequestService serviceRequestService;
    private final MQConsumerUtils mqConsumerUtils;
    private final Channel responseChannel;
    private final QueryService queryService;
    private final ObjectMapper objectMapper;

    public MQConsumers(Connection connection, MQConsumerUtils mqConsumerUtils, QueryService queryService, ServiceRequestService serviceRequestService) throws IOException {
        this.serviceRequestService = serviceRequestService;
        this.mqConsumerUtils = mqConsumerUtils;
        this.queryService = queryService;
        this.objectMapper = new ObjectMapper().findAndRegisterModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        mqConsumerUtils.bindConsumer(connection, mqConsumerUtils.getExchange(), mqConsumerUtils.getServicerequestQueue(), true, this::receivedServiceRequestMessage);
        mqConsumerUtils.bindConsumer(connection, mqConsumerUtils.getExchange(), mqConsumerUtils.getServicerequestQueryIdQueue(), true, this::receivedServiceRequestQueryIdMessage);
        mqConsumerUtils.bindConsumer(connection, mqConsumerUtils.getExchange(), mqConsumerUtils.getServiceRequestCheckRequestQueue(), true, this::receivedCheckRequestMessage);
        mqConsumerUtils.bindConsumer(connection, mqConsumerUtils.getExchange(), mqConsumerUtils.getServiceRequestBillLoanQueue(), true, this::receivedServiceRequestBillloanMessage);
        mqConsumerUtils.bindConsumer(connection, mqConsumerUtils.getExchange(), mqConsumerUtils.getServiceRequestStatementCompleteQueue(), true, this::receivedServiceStatementCompleteMessage);

        responseChannel = connection.createChannel();
    }

    public void receivedServiceRequestQueryIdMessage(String consumerTag, Delivery delivery) {
        try {
            UUID id = (UUID) SerializationUtils.deserialize(delivery.getBody());
            log.debug("ServiceRequestQueryId Received {}", id);
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
            reply(delivery, response);
        } catch (Exception e) {
            log.error("receivedCheckRequestMessage", e);
        }
    }


    public void receivedCheckRequestMessage(String consumerTag, Delivery delivery) {
        log.debug("CheckRequest Received");
        try {
            reply(delivery, queryService.checkRequest());
        } catch (Exception e) {
            log.error("receivedCheckRequestMessage", e);
        }
    }

    private void reply(Delivery delivery, Object data) throws IOException {
        AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                .Builder()
                .correlationId(delivery.getProperties().getCorrelationId())
                .build();
        responseChannel.basicPublish(mqConsumerUtils.getExchange(), delivery.getProperties().getReplyTo(), replyProps, SerializationUtils.serialize(data));

    }

    public void receivedServiceRequestMessage(String consumerTag, Delivery delivery) {
        ServiceRequestResponse serviceRequest = (ServiceRequestResponse) SerializationUtils.deserialize(delivery.getBody());
        log.debug("ServiceRequestResponse Received {}", serviceRequest);
        try {
            serviceRequestService.completeServiceRequest(serviceRequest);
        } catch (SQLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public void receivedServiceRequestBillloanMessage(String consumerTag, Delivery delivery) {
        BillingCycle billingCycle = (BillingCycle) SerializationUtils.deserialize(delivery.getBody());
        if ( billingCycle == null ) {
            throw new IllegalStateException("Message body null");
        }
        log.debug("Billloan Received {}", billingCycle);
        try {
            serviceRequestService.statementStatementRequest(StatementRequest.builder()
                            .loanId(billingCycle.getLoanId())
                            .statementDate(billingCycle.getStatementDate())
                            .startDate(billingCycle.getStartDate())
                            .endDate(billingCycle.getEndDate())
                            .build(),
                    Boolean.FALSE, null);
        } catch ( ServiceRequestException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public void receivedServiceStatementCompleteMessage(String consumerTag, Delivery delivery) {
        StatementCompleteResponse statementCompleteResponse = (StatementCompleteResponse) SerializationUtils.deserialize(delivery.getBody());
        log.debug("StatementComplete Received {}", statementCompleteResponse);
        try {
            serviceRequestService.statementComplete(statementCompleteResponse);
        } catch (SQLException ex) {
            throw new IllegalStateException(ex);
        }
    }

}