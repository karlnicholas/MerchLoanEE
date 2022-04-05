package com.github.karlnicholas.merchloan.servicerequest.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.karlnicholas.merchloan.apimessage.message.StatementRequest;
import com.github.karlnicholas.merchloan.jmsmessage.BillingCycle;
import com.github.karlnicholas.merchloan.jmsmessage.ServiceRequestResponse;
import com.github.karlnicholas.merchloan.jmsmessage.StatementCompleteResponse;
import com.github.karlnicholas.merchloan.servicerequest.service.QueryService;
import com.github.karlnicholas.merchloan.servicerequest.service.ServiceRequestService;
import com.rabbitmq.client.Delivery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.io.IOException;

@Component
@Slf4j
public class RabbitMqReceiver {
    private final ServiceRequestService serviceRequestService;

    public RabbitMqReceiver(ServiceRequestService serviceRequestService) {
        this.serviceRequestService = serviceRequestService;
    }


    public void receivedServiceRequestMessage(String consumerTag, Delivery delivery) {
        ServiceRequestResponse serviceRequest = (ServiceRequestResponse) SerializationUtils.deserialize(delivery.getBody());
        log.debug("ServiceRequestResponse Received {}", serviceRequest);
        serviceRequestService.completeServiceRequest(serviceRequest);
    }

    public void receivedServiceRequestBillloanMessage(String consumerTag, Delivery delivery) throws IOException {
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