package com.github.karlnicholas.merchloan.register.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.karlnicholas.merchloan.jmsmessage.CreditAccount;
import com.github.karlnicholas.merchloan.jmsmessage.DebitAccount;
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

    public RabbitMqReceiver(RegisterManagementService registerManagementService, QueryService queryService) {
        this.registerManagementService = registerManagementService;
        this.queryService = queryService;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar rabbitListenerEndpointRegistrar) {
    }

    @RabbitListener(queues = "${rabbitmq.register.creditaccount.queue}")
    public void receivedCreditAccountMessage(CreditAccount creditAccount) {
        try {
            log.info("CreditAccount Received {}", creditAccount);
            registerManagementService.creditAccount(creditAccount);
        } catch ( Exception ex) {
            log.error("void receivedCreditMessage(CreditAccount creditAccount) exception {}", ex.getMessage());
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }

    @RabbitListener(queues = "${rabbitmq.register.debitaccount.queue}")
    public void receivedDebitAccountMessage(DebitAccount debitAccount) {
        try {
            log.info("DebitAccount Received {}", debitAccount);
            registerManagementService.debitAccount(debitAccount);
        } catch ( Exception ex) {
            log.error("void receivedDebitAccountMessage(DebitAccount debitAccount) exception {}", ex.getMessage());
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