package com.github.karlnicholas.merchloan.servicerequest.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.karlnicholas.merchloan.apimessage.message.StatementRequest;
import com.github.karlnicholas.merchloan.dto.RequestStatusDto;
import com.github.karlnicholas.merchloan.jms.config.MQQueueNames;
import com.github.karlnicholas.merchloan.jmsmessage.BillingCycle;
import com.github.karlnicholas.merchloan.jmsmessage.ServiceRequestResponse;
import com.github.karlnicholas.merchloan.jmsmessage.StatementCompleteResponse;
import com.github.karlnicholas.merchloan.servicerequest.model.ServiceRequest;
import com.github.karlnicholas.merchloan.servicerequest.service.QueryService;
import com.github.karlnicholas.merchloan.servicerequest.service.ServiceRequestService;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class MQConsumers {
    private final ServiceRequestService serviceRequestService;
    private final MQQueueNames MQQueueNames;
    private final Channel responseChannel;
    private final QueryService queryService;
    private final ObjectMapper objectMapper;

    public MQConsumers(Connection connection, MQQueueNames MQQueueNames, QueryService queryService, ServiceRequestService serviceRequestService) throws IOException {
        this.serviceRequestService = serviceRequestService;
        this.MQQueueNames = MQQueueNames;
        this.queryService = queryService;
        this.objectMapper = new ObjectMapper().findAndRegisterModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        Channel servicerequestChannel = connection.createChannel();
        servicerequestChannel.exchangeDeclare(MQQueueNames.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        servicerequestChannel.queueDeclare(MQQueueNames.getServicerequestQueue(), false, true, true, null);
        servicerequestChannel.queueBind(MQQueueNames.getServicerequestQueue(), MQQueueNames.getExchange(), MQQueueNames.getServicerequestQueue());
        servicerequestChannel.basicConsume(MQQueueNames.getServicerequestQueue(), true, this::receivedServiceRequestMessage, consumerTag -> {
        });

        Channel servicerequestQueryIdChannel = connection.createChannel();
        servicerequestQueryIdChannel.exchangeDeclare(MQQueueNames.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        servicerequestQueryIdChannel.queueDeclare(MQQueueNames.getServicerequestQueryIdQueue(), false, true, true, null);
        servicerequestQueryIdChannel.queueBind(MQQueueNames.getServicerequestQueryIdQueue(), MQQueueNames.getExchange(), MQQueueNames.getServicerequestQueryIdQueue());
        servicerequestQueryIdChannel.basicConsume(MQQueueNames.getServicerequestQueryIdQueue(), true, this::receivedServiceRequestQueryIdMessage, consumerTag -> {
        });

        Channel serviceRequestCheckRequestChannel = connection.createChannel();
        serviceRequestCheckRequestChannel.exchangeDeclare(MQQueueNames.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        serviceRequestCheckRequestChannel.queueDeclare(MQQueueNames.getServiceRequestCheckRequestQueue(), false, true, true, null);
        serviceRequestCheckRequestChannel.queueBind(MQQueueNames.getServiceRequestCheckRequestQueue(), MQQueueNames.getExchange(), MQQueueNames.getServiceRequestCheckRequestQueue());
        serviceRequestCheckRequestChannel.basicConsume(MQQueueNames.getServiceRequestCheckRequestQueue(), true, this::receivedCheckRequestMessage, consumerTag -> {
        });

        Channel serviceRequestBillLoanChannel = connection.createChannel();
        serviceRequestBillLoanChannel.exchangeDeclare(MQQueueNames.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        serviceRequestBillLoanChannel.queueDeclare(MQQueueNames.getServiceRequestBillLoanQueue(), false, true, true, null);
        serviceRequestBillLoanChannel.queueBind(MQQueueNames.getServiceRequestBillLoanQueue(), MQQueueNames.getExchange(), MQQueueNames.getServiceRequestBillLoanQueue());
        serviceRequestBillLoanChannel.basicConsume(MQQueueNames.getServiceRequestBillLoanQueue(), true, this::receivedServiceRequestBillloanMessage, consumerTag -> {
        });

        Channel serviceRequestStatementCompleteQueue = connection.createChannel();
        serviceRequestStatementCompleteQueue.exchangeDeclare(MQQueueNames.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        serviceRequestStatementCompleteQueue.queueDeclare(MQQueueNames.getServiceRequestStatementCompleteQueue(), false, true, true, null);
        serviceRequestStatementCompleteQueue.queueBind(MQQueueNames.getServiceRequestStatementCompleteQueue(), MQQueueNames.getExchange(), MQQueueNames.getServiceRequestStatementCompleteQueue());
        serviceRequestStatementCompleteQueue.basicConsume(MQQueueNames.getServiceRequestStatementCompleteQueue(), true, this::receivedServiceStatementCompleteMessage, consumerTag -> {
        });

        responseChannel = connection.createChannel();
    }

    public void receivedServiceRequestQueryIdMessage(String consumerTag, Delivery delivery) throws IOException {
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


    public void receivedCheckRequestMessage(String consumerTag, Delivery delivery) throws IOException {
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
        responseChannel.basicPublish(MQQueueNames.getExchange(), delivery.getProperties().getReplyTo(), replyProps, SerializationUtils.serialize(data));

    }

    public void receivedServiceRequestMessage(String consumerTag, Delivery delivery) {
        ServiceRequestResponse serviceRequest = (ServiceRequestResponse) SerializationUtils.deserialize(delivery.getBody());
        log.debug("ServiceRequestResponse Received {}", serviceRequest);
        serviceRequestService.completeServiceRequest(serviceRequest);
    }

    public void receivedServiceRequestBillloanMessage(String consumerTag, Delivery delivery) {
        BillingCycle billingCycle = (BillingCycle) SerializationUtils.deserialize(delivery.getBody());
        log.debug("Billloan Received {}", billingCycle);
        serviceRequestService.statementStatementRequest(StatementRequest.builder()
                        .loanId(billingCycle.getLoanId())
                        .statementDate(billingCycle.getStatementDate())
                        .startDate(billingCycle.getStartDate())
                        .endDate(billingCycle.getEndDate())
                        .build(),
                Boolean.FALSE, null);
    }

    public void receivedServiceStatementCompleteMessage(String consumerTag, Delivery delivery) {
        StatementCompleteResponse statementCompleteResponse = (StatementCompleteResponse) SerializationUtils.deserialize(delivery.getBody());
        log.debug("StatementComplete Received {}", statementCompleteResponse);
        serviceRequestService.statementComplete(statementCompleteResponse);
    }

}