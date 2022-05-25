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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.jms.*;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class MQConsumers {
    private final ServiceRequestService serviceRequestService;
    private final QueryService queryService;
    private final ObjectMapper objectMapper;
    private final JMSContext servicerequestContext;
    private final JMSContext servicerequestQueryIdContext;
//    private final JMSProducer servicerequestQueryIdProducer;
    private final JMSContext serviceRequestCheckRequestContext;
//    private final JMSProducer serviceRequestCheckRequestProducer;
    private final JMSContext serviceRequestBillLoanContext;
    private final JMSContext serviceRequestStatementCompleteContext;

    public MQConsumers(ConnectionFactory connectionFactory, MQConsumerUtils mqConsumerUtils, QueryService queryService, ServiceRequestService serviceRequestService) throws JMSException {
        this.serviceRequestService = serviceRequestService;
        this.queryService = queryService;
        this.objectMapper = new ObjectMapper().findAndRegisterModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        servicerequestContext = connectionFactory.createContext();
        servicerequestContext.setClientID("ServiceRequest::servicerequestContext");
        servicerequestContext.createConsumer(servicerequestContext.createQueue(mqConsumerUtils.getServicerequestQueue())).setMessageListener(this::receivedServiceRequestMessage);

        servicerequestQueryIdContext = connectionFactory.createContext();
        servicerequestQueryIdContext.setClientID("ServiceRequest::servicerequestQueryIdContext");
        servicerequestQueryIdContext.createConsumer(servicerequestQueryIdContext.createQueue(mqConsumerUtils.getServicerequestQueryIdQueue())).setMessageListener(this::receivedServiceRequestQueryIdMessage);
//        servicerequestQueryIdProducer = servicerequestQueryIdContext.createProducer();

        serviceRequestCheckRequestContext = connectionFactory.createContext();
        serviceRequestCheckRequestContext.setClientID("ServiceRequest::serviceRequestCheckRequestContext");
        serviceRequestCheckRequestContext.createConsumer(serviceRequestCheckRequestContext.createQueue(mqConsumerUtils.getServiceRequestCheckRequestQueue())).setMessageListener(this::receivedCheckRequestMessage);
//        serviceRequestCheckRequestProducer = serviceRequestCheckRequestContext.createProducer();

        serviceRequestBillLoanContext = connectionFactory.createContext();
        serviceRequestBillLoanContext.setClientID("ServiceRequest::serviceRequestBillLoanContext");
        serviceRequestBillLoanContext.createConsumer(serviceRequestBillLoanContext.createQueue(mqConsumerUtils.getServiceRequestBillLoanQueue())).setMessageListener(this::receivedServiceRequestBillloanMessage);

        serviceRequestStatementCompleteContext = connectionFactory.createContext();
        serviceRequestStatementCompleteContext.setClientID("ServiceRequest::serviceRequestStatementCompleteContext");
        serviceRequestStatementCompleteContext.createConsumer(serviceRequestStatementCompleteContext.createQueue(mqConsumerUtils.getServiceRequestStatementCompleteQueue())).setMessageListener(this::receivedServiceStatementCompleteMessage);
    }

    public void receivedServiceRequestQueryIdMessage(Message message) {
        try {
            UUID id = (UUID) ((ObjectMessage) message).getObject();
            log.debug("receivedServiceRequestQueryIdMessage: {}", id);
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
            reply(servicerequestQueryIdContext, message, response);
        } catch (Exception e) {
            log.error("receivedCheckRequestMessage", e);
        }
    }


    public void receivedCheckRequestMessage(Message message) {
        log.trace("receivedCheckRequestMessage");
        try {
            reply(serviceRequestCheckRequestContext, message, queryService.checkRequest());
        } catch (Exception e) {
            log.error("receivedCheckRequestMessage", e);
        }
    }

    public void reply(JMSContext context, Message consumerMessage, Serializable data) throws JMSException {
        Message message = context.createObjectMessage(data);
        message.setJMSCorrelationID(consumerMessage.getJMSCorrelationID());
        context.createProducer().send(consumerMessage.getJMSReplyTo(), message);
    }

    public void receivedServiceRequestMessage(Message message) {
        try {
            ServiceRequestResponse serviceRequest = (ServiceRequestResponse) ((ObjectMessage) message).getObject();
            log.debug("receivedServiceRequestMessage {}", serviceRequest);
            serviceRequestService.completeServiceRequest(serviceRequest);
        } catch (SQLException | JMSException ex) {
            log.error("receivedServiceStatementCompleteMessage", ex);
        }
    }

    public void receivedServiceRequestBillloanMessage(Message message) {
        try {
            BillingCycle billingCycle = (BillingCycle) ((ObjectMessage) message).getObject();
            log.debug("receivedServiceRequestBillloanMessage: {}", billingCycle);
            serviceRequestService.statementStatementRequest(StatementRequest.builder()
                            .loanId(billingCycle.getLoanId())
                            .statementDate(billingCycle.getStatementDate())
                            .startDate(billingCycle.getStartDate())
                            .endDate(billingCycle.getEndDate())
                            .build(),
                    Boolean.FALSE, null);
        } catch (ServiceRequestException | JMSException ex) {
            log.error("receivedServiceStatementCompleteMessage", ex);
        }
    }

    public void receivedServiceStatementCompleteMessage(Message message) {
        try {
            StatementCompleteResponse statementCompleteResponse = (StatementCompleteResponse) ((ObjectMessage) message).getObject();
            log.debug("receivedServiceStatementCompleteMessage: {}", statementCompleteResponse);
            serviceRequestService.statementComplete(statementCompleteResponse);
        } catch (SQLException | JMSException ex) {
            log.error("receivedServiceStatementCompleteMessage", ex);
        }
    }

}