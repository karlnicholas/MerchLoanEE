package com.github.karlnicholas.merchloan.accounts.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.karlnicholas.merchloan.accounts.model.Account;
import com.github.karlnicholas.merchloan.accounts.model.Loan;
import com.github.karlnicholas.merchloan.accounts.service.QueryService;
import com.github.karlnicholas.merchloan.jmsmessage.CreateAccount;
import com.github.karlnicholas.merchloan.accounts.service.AccountManagementService;
import com.github.karlnicholas.merchloan.jmsmessage.CreditLoan;
import com.github.karlnicholas.merchloan.jmsmessage.DebitLoan;
import com.github.karlnicholas.merchloan.jmsmessage.FundLoan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class RabbitMqReceiver implements RabbitListenerConfigurer {
    private final AccountManagementService accountManagementService;
    private final QueryService queryService;
    private final ObjectMapper objectMapper;

    public RabbitMqReceiver(AccountManagementService accountManagementService, QueryService queryService) {
        this.accountManagementService = accountManagementService;
        this.queryService = queryService;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar rabbitListenerEndpointRegistrar) {
    }

    @RabbitListener(queues = "${rabbitmq.account.createaccount.queue}")
    public void receivedAccountMessage(CreateAccount createAccount) {
        log.info("CreateAccount Details Received is.. {}", createAccount);
        accountManagementService.createAccount(createAccount);
    }

    @RabbitListener(queues = "${rabbitmq.account.funding.queue}")
    public void receivedFundingMessage(FundLoan funding) {
        try {
            log.info("FundLoan Received {} ", funding);
            accountManagementService.fundAccount(funding);
        } catch ( Exception ex) {
            log.error("void receivedFundingMessage(FundLoan funding) {}", ex.getMessage());
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }

    @RabbitListener(queues = "${rabbitmq.account.validate.credit.queue}")
    public void receivedValidateCreditMessage(CreditLoan creditLoan) {
        try {
            log.info("CreditLoan Received {} ", creditLoan);
            accountManagementService.validateCreditLoad(creditLoan);
        } catch ( Exception ex) {
            log.error("void receivedValidateCreditMessage(CreditLoan creditLoan) {}", ex.getMessage());
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }

    @RabbitListener(queues = "${rabbitmq.account.validate.debit.queue}")
    public void receivedValidateDebitMessage(DebitLoan debitLoan) {
        try {
            log.info("DebitLoan Received {} ", debitLoan);
            accountManagementService.validateDebitLoad(debitLoan);
        } catch ( Exception ex) {
            log.error("void receivedValidateDebitMessage(DebitLoan debitLoan) {}", ex.getMessage());
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }

    @RabbitListener(queues = "${rabbitmq.account.query.account.id.queue}")
    public String receivedQueryAccountIdMessage(UUID id) {
        try {
            log.info("QueryAccountId Received {}}", id);
            Optional<Account> r = queryService.queryAccountId(id);
            if ( r.isPresent() ) {
                return objectMapper.writeValueAsString(r.get());
            } else {
                return "ERROR: id not found: " + id;
            }
        } catch ( Exception ex) {
            log.error("String receivedQueryAccountIdMessage(UUID id) exception {}", ex.getMessage());
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }

    @RabbitListener(queues = "${rabbitmq.account.query.loan.id.queue}")
    public String receivedQueryLoanIdMessage(UUID id) {
        try {
            log.info("QueryLoanId Received {}", id);
            Optional<Loan> r = queryService.queryLoanId(id);
            if ( r.isPresent() ) {
                return objectMapper.writeValueAsString(r.get());
            } else {
                return "ERROR: id not found: " + id;
            }
        } catch ( Exception ex) {
            log.error("String receivedQueryLoanIdMessage(UUID id) exception {}", ex.getMessage());
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }
}