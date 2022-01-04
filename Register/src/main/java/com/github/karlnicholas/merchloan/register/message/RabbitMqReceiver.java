package com.github.karlnicholas.merchloan.register.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.karlnicholas.merchloan.jms.message.RabbitMqSender;
import com.github.karlnicholas.merchloan.jmsmessage.CreditLoan;
import com.github.karlnicholas.merchloan.jmsmessage.DebitLoan;
import com.github.karlnicholas.merchloan.jmsmessage.FundLoan;
import com.github.karlnicholas.merchloan.jmsmessage.ServiceRequestResponse;
import com.github.karlnicholas.merchloan.register.model.RegisterEntry;
import com.github.karlnicholas.merchloan.register.service.RegisterManagementService;
import com.github.karlnicholas.merchloan.register.service.QueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class RabbitMqReceiver implements RabbitListenerConfigurer {
    private final RegisterManagementService registerManagementService;
    private final QueryService queryService;
    private final ObjectMapper objectMapper;
    private final RabbitMqSender rabbitMqSender;

    public RabbitMqReceiver(RegisterManagementService registerManagementService, QueryService queryService, RabbitMqSender rabbitMqSender) {
        this.registerManagementService = registerManagementService;
        this.queryService = queryService;
        this.rabbitMqSender = rabbitMqSender;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar rabbitListenerEndpointRegistrar) {
    }

    @RabbitListener(queues = "${rabbitmq.register.fundloan.queue}")
    public void receivedFundLoanMessage(DebitLoan debitLoan) {
        try {
            log.info("FundLoan Received {}", debitLoan);
            ServiceRequestResponse serviceRequestResponse = registerManagementService.fundLoan(debitLoan);
            rabbitMqSender.serviceRequestServiceRequest(serviceRequestResponse);
        } catch ( Exception ex) {
            log.error("void receivedCreditLoanMessage(CreditLoan creditLoan) exception {}", ex.getMessage());
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }

    @RabbitListener(queues = "${rabbitmq.register.creditloan.queue}")
    public void receivedCreditLoanMessage(CreditLoan creditLoan) {
        try {
            log.info("CreditLoan Received {}", creditLoan);
            ServiceRequestResponse serviceRequestResponse = registerManagementService.creditLoan(creditLoan);
            rabbitMqSender.serviceRequestServiceRequest(serviceRequestResponse);
        } catch ( Exception ex) {
            log.error("void receivedCreditLoanMessage(CreditLoan creditLoan) exception {}", ex.getMessage());
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }

    @RabbitListener(queues = "${rabbitmq.register.debitloan.queue}")
    public void receivedDebitLoanMessage(DebitLoan debitLoan) {
        try {
            log.info("DebitLoan Received {}", debitLoan);
            ServiceRequestResponse serviceRequestResponse = registerManagementService.debitLoan(debitLoan);
            rabbitMqSender.serviceRequestServiceRequest(serviceRequestResponse);
        } catch ( Exception ex) {
            log.error("void receivedDebitLoanMessage(DebitLoan debitLoan) exception {}", ex.getMessage());
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }

    @RabbitListener(queues = "${rabbitmq.register.query.loan.id.queue}")
    public String receivedQueryLoanIdMessage(UUID id) {
        try {
            log.info("QueryLoanId Received {}", id);
            List<? extends RegisterEntry> es = queryService.queryRegisterByLoanId(id);
            return objectMapper.writeValueAsString(es);
        } catch ( Exception ex) {
            log.error("String receivedQueryLoanIdMessage(UUID id) exception {}", ex.getMessage());
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }
}