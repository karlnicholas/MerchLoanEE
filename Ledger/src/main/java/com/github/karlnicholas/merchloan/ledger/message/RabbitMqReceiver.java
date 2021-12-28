package com.github.karlnicholas.merchloan.ledger.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.karlnicholas.merchloan.jms.message.RabbitMqSender;
import com.github.karlnicholas.merchloan.jmsmessage.CreditAccount;
import com.github.karlnicholas.merchloan.jmsmessage.DebitAccount;
import com.github.karlnicholas.merchloan.jmsmessage.ServiceRequestResponse;
import com.github.karlnicholas.merchloan.ledger.model.LedgerEntry;
import com.github.karlnicholas.merchloan.ledger.service.LedgerManagementService;
import com.github.karlnicholas.merchloan.ledger.service.QueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class RabbitMqReceiver implements RabbitListenerConfigurer {
    private final LedgerManagementService ledgerManagementService;
    private final QueryService queryService;
    private final RabbitMqSender rabbitMqSender;
    private final ObjectMapper objectMapper;

    public RabbitMqReceiver(LedgerManagementService ledgerManagementService, QueryService queryService, RabbitMqSender rabbitMqSender) {
        this.ledgerManagementService = ledgerManagementService;
        this.queryService = queryService;
        this.rabbitMqSender = rabbitMqSender;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar rabbitListenerEndpointRegistrar) {
    }

    @RabbitListener(queues = "${rabbitmq.ledger.creditaccount.queue}")
    public void receivedCreditAccountMessage(CreditAccount creditAccount) {
        try {
            log.info("CreditAccount Received {}", creditAccount);
            try {
                ledgerManagementService.creditAccount(creditAccount);
                rabbitMqSender.sendServiceRequest(ServiceRequestResponse.builder()
                        .id(creditAccount.getId())
                        .status("Success")
                        .statusMessage("Success")
                        .build());
            } catch (DuplicateKeyException dke) {
                log.warn("CreditEntry creditAccount(CreditAccount creditAccount) duplicate key: {}", dke.getMessage());
                rabbitMqSender.sendServiceRequest(ServiceRequestResponse.builder()
                        .id(creditAccount.getId())
                        .status("Failure")
                        .statusMessage(dke.getMessage())
                        .build());
            }
        } catch ( Exception ex) {
            log.error("void receivedCreditMessage(CreditAccount creditAccount) exception {}", ex.getMessage());
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }

    @RabbitListener(queues = "${rabbitmq.ledger.debitaccount.queue}")
    public void receivedDebitAccountMessage(DebitAccount debitAccount) {
        try {
            log.info("DebitAccount Received {}", debitAccount);
            try {
                ledgerManagementService.debitAccount(debitAccount);
                rabbitMqSender.sendServiceRequest(ServiceRequestResponse.builder()
                        .id(debitAccount.getId())
                        .status("Success")
                        .statusMessage("Success")
                        .build());
            } catch (DuplicateKeyException dke) {
                log.warn("DebitEntry debitAccount(DebitAccount debitAccount) duplicate key: {}", dke.getMessage());
                rabbitMqSender.sendServiceRequest(ServiceRequestResponse.builder()
                        .id(debitAccount.getId())
                        .status("Failure")
                        .statusMessage(dke.getMessage())
                        .build());
            }
        } catch ( Exception ex) {
            log.error("void receivedDebitAccountMessage(DebitAccount debitAccount) exception {}", ex.getMessage());
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }

    @RabbitListener(queues = "${rabbitmq.ledger.query.loan.id.queue}")
    public String receivedQueryLoanIdMessage(UUID id) {
        try {
            log.info("QueryLoanId Received {}", id);
            List<? extends LedgerEntry> es = queryService.queryLedgerByLoanId(id);
            return objectMapper.writeValueAsString(es);
        } catch ( Exception ex) {
            log.error("String receivedQueryLoanIdMessage(UUID id) exception {}", ex.getMessage());
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }
}