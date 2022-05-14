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

import javax.jms.*;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class MQConsumers {
    private final Session session;
    private final ServiceRequestService serviceRequestService;
    private final MessageProducer responseProducer;
    private final QueryService queryService;
    private final ObjectMapper objectMapper;

    public MQConsumers(Session session, MQConsumerUtils mqConsumerUtils, QueryService queryService, ServiceRequestService serviceRequestService) throws JMSException {
        this.session = session;
        this.serviceRequestService = serviceRequestService;
        this.queryService = queryService;
        this.objectMapper = new ObjectMapper().findAndRegisterModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        mqConsumerUtils.bindConsumer(session, session.createQueue(mqConsumerUtils.getServicerequestQueue()), this::receivedServiceRequestMessage);
        mqConsumerUtils.bindConsumer(session, session.createQueue(mqConsumerUtils.getServicerequestQueryIdQueue()), this::receivedServiceRequestQueryIdMessage);
        mqConsumerUtils.bindConsumer(session, session.createQueue(mqConsumerUtils.getServiceRequestCheckRequestQueue()), this::receivedCheckRequestMessage);
        mqConsumerUtils.bindConsumer(session, session.createQueue(mqConsumerUtils.getServiceRequestBillLoanQueue()), this::receivedServiceRequestBillloanMessage);
        mqConsumerUtils.bindConsumer(session, session.createQueue(mqConsumerUtils.getServiceRequestStatementCompleteQueue()), this::receivedServiceStatementCompleteMessage);

        responseProducer = session.createProducer(null);
    }

    public void receivedServiceRequestQueryIdMessage(Message message) {
        try {
            UUID id = (UUID) ((ObjectMessage) message).getObject();
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
            reply(message, response);
        } catch (Exception e) {
            log.error("receivedCheckRequestMessage", e);
        }
    }


    public void receivedCheckRequestMessage(Message message) {
        log.debug("CheckRequest Received");
        try {
            reply(message, queryService.checkRequest());
        } catch (Exception e) {
            log.error("receivedCheckRequestMessage", e);
        }
    }

    public void reply(Message consumerMessage, Serializable data) throws JMSException {
        Message message = session.createObjectMessage(data);
        message.setJMSCorrelationID(consumerMessage.getJMSCorrelationID());
        responseProducer.send(consumerMessage.getJMSReplyTo(), message);
    }

    public void receivedServiceRequestMessage(Message message) {
        try {
            ServiceRequestResponse serviceRequest = (ServiceRequestResponse) ((ObjectMessage) message).getObject();
            log.debug("ServiceRequestResponse Received {}", serviceRequest);
            serviceRequestService.completeServiceRequest(serviceRequest);
        } catch (SQLException | JMSException ex) {
            throw new java.lang.IllegalStateException(ex);
        }
    }

    public void receivedServiceRequestBillloanMessage(Message message) {
        try {
            BillingCycle billingCycle = (BillingCycle) ((ObjectMessage) message).getObject();
            log.debug("Billloan Received {}", billingCycle);
            serviceRequestService.statementStatementRequest(StatementRequest.builder()
                            .loanId(billingCycle.getLoanId())
                            .statementDate(billingCycle.getStatementDate())
                            .startDate(billingCycle.getStartDate())
                            .endDate(billingCycle.getEndDate())
                            .build(),
                    Boolean.FALSE, null);
        } catch (ServiceRequestException | JMSException ex) {
            throw new java.lang.IllegalStateException(ex);
        }
    }

    public void receivedServiceStatementCompleteMessage(Message message) {
        try {
            StatementCompleteResponse statementCompleteResponse = (StatementCompleteResponse) ((ObjectMessage) message).getObject();
            log.debug("StatementComplete Received {}", statementCompleteResponse);
            serviceRequestService.statementComplete(statementCompleteResponse);
        } catch (SQLException | JMSException ex) {
            throw new java.lang.IllegalStateException(ex);
        }
    }

}