package com.github.karlnicholas.merchloan.servicerequest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.karlnicholas.merchloan.apimessage.message.*;
import com.github.karlnicholas.merchloan.jms.message.RabbitMqSender;
import com.github.karlnicholas.merchloan.jmsmessage.*;
import com.github.karlnicholas.merchloan.redis.component.RedisComponent;
import com.github.karlnicholas.merchloan.servicerequest.model.ServiceRequest;
import com.github.karlnicholas.merchloan.servicerequest.repository.ServiceRequestRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class ServiceRequestService {
    private final RabbitMqSender rabbitMqSender;
    private final ServiceRequestRepository serviceRequestRepository;
    private final ObjectMapper objectMapper;
    private final RedisComponent redisComponent;

    public ServiceRequestService(RabbitMqSender rabbitMqSender, ServiceRequestRepository serviceRequestRepository, ObjectMapper objectMapper, RedisComponent redisComponent) {
        this.rabbitMqSender = rabbitMqSender;
        this.serviceRequestRepository = serviceRequestRepository;
        this.objectMapper = objectMapper;
        this.redisComponent = redisComponent;
    }

    public UUID accountRequest(ServiceRequestMessage serviceRequestMessage, Boolean retry) throws JsonProcessingException {
        AccountRequest accountRequest = (AccountRequest) serviceRequestMessage;
        UUID id = persistRequest(accountRequest);
        try {
            rabbitMqSender.accountCreateAccount(CreateAccount.builder()
                    .id(id)
                    .customer(accountRequest.getCustomer())
                    .createDate(redisComponent.getBusinessDate())
                    .retry(retry)
                    .build());
        } catch (Exception e) {
            log.error("Send account create message failed: {}", e.getMessage());
        }
        return id;
    }

    public UUID fundingRequest(ServiceRequestMessage serviceRequestMessage, Boolean retry) throws JsonProcessingException {
        FundingRequest fundingRequest = (FundingRequest) serviceRequestMessage;
        UUID id = persistRequest(fundingRequest);
        rabbitMqSender.accountFundLoan(
                FundLoan.builder()
                        .id(id)
                        .accountId(fundingRequest.getAccountId())
                        .amount(fundingRequest.getAmount())
                        .startDate(redisComponent.getBusinessDate())
                        .description(fundingRequest.getDescription())
                        .retry(retry)
                        .build()
        );
        return id;
    }

    public UUID accountValidateCreditRequest(ServiceRequestMessage serviceRequestMessage, Boolean retry) throws JsonProcessingException {
        CreditRequest creditRequest = (CreditRequest) serviceRequestMessage;
        UUID id = persistRequest(creditRequest);
        rabbitMqSender.accountValidateCredit(
                CreditLoan.builder()
                        .id(id)
                        .loanId(creditRequest.getLoanId())
                        .date(redisComponent.getBusinessDate())
                        .amount(creditRequest.getAmount())
                        .description(creditRequest.getDescription())
                        .retry(retry)
                        .build()
        );
        return id;
    }

    public UUID statementStatementRequest(ServiceRequestMessage serviceRequestMessage, Boolean retry) throws JsonProcessingException {
        StatementRequest statementRequest = (StatementRequest) serviceRequestMessage;
        UUID id = persistRequest(statementRequest);
        rabbitMqSender.statementStatement(
                StatementHeader.builder()
                        .id(id)
                        .loanId(statementRequest.getLoanId())
                        .interestChargeId(UUID.randomUUID())
                        .feeChargeId(UUID.randomUUID())
                        .statementDate(statementRequest.getStatementDate())
                        .startDate(statementRequest.getStartDate())
                        .endDate(statementRequest.getEndDate())
                        .retry(retry)
                        .build()
        );
        return id;
    }
    public UUID closeRequest(ServiceRequestMessage serviceRequestMessage, Boolean retry) throws JsonProcessingException {
        CloseRequest closeRequest = (CloseRequest) serviceRequestMessage;
        UUID id = persistRequest(closeRequest);
        rabbitMqSender.accountCloseLoan(
                CloseLoan.builder()
                        .id(id)
                        .loanId(closeRequest.getLoanId())
                        .interestChargeId(UUID.randomUUID())
                        .paymentId(UUID.randomUUID())
                        .date(redisComponent.getBusinessDate())
                        .amount(closeRequest.getAmount())
                        .description(closeRequest.getDescription())
                        .retry(retry)
                        .build()
        );
        return id;
    }

    public UUID accountValidateDebitRequest(ServiceRequestMessage serviceRequestMessage, Boolean retry) throws JsonProcessingException {
        DebitRequest debitRequest = (DebitRequest) serviceRequestMessage;
        UUID id = persistRequest(debitRequest);
        rabbitMqSender.accountValidateDebit(
                DebitLoan.builder()
                        .id(id)
                        .loanId(debitRequest.getLoanId())
                        .date(redisComponent.getBusinessDate())
                        .amount(debitRequest.getAmount())
                        .description(debitRequest.getDescription())
                        .retry(retry)
                        .build()
        );
        return id;
    }

    public UUID billingCycleChargeRequest(ServiceRequestMessage serviceRequestMessage, Boolean retry) throws JsonProcessingException {
        BillingCycleChargeRequest billingCycleChargeRequest = (BillingCycleChargeRequest) serviceRequestMessage;
        UUID id = billingCycleChargeRequest.getId();
        persistRequestWithId(billingCycleChargeRequest, id);
        if ( billingCycleChargeRequest.getDebitRequest() != null ) {
            rabbitMqSender.registerBillingCycleCharge(BillingCycleCharge.builder()
                    .id(id)
                    .loanId(billingCycleChargeRequest.getDebitRequest().getLoanId())
                    .date(billingCycleChargeRequest.getDate())
                    .debit(billingCycleChargeRequest.getDebitRequest().getAmount())
                    .description(billingCycleChargeRequest.getDebitRequest().getDescription())
                    .retry(retry)
                    .build());

        } else {
            rabbitMqSender.registerBillingCycleCharge(BillingCycleCharge.builder()
                    .id(id)
                    .loanId(billingCycleChargeRequest.getCreditRequest().getLoanId())
                    .date(billingCycleChargeRequest.getDate())
                    .credit(billingCycleChargeRequest.getCreditRequest().getAmount())
                    .description(billingCycleChargeRequest.getCreditRequest().getDescription())
                    .retry(retry)
                    .build());
        }
        return id;
    }

    public void chargeCompleted(BillingCycleCharge billingCycleCharge) {
        completeServiceRequest(ServiceRequestResponse.builder()
                .id(billingCycleCharge.getId())
                .status(ServiceRequestMessage.STATUS.SUCCESS)
                .statusMessage(ServiceRequestMessage.STATUS.SUCCESS.name())
                .build());
        redisComponent.chargeCompleted(billingCycleCharge);
    }

    private UUID persistRequest(ServiceRequestMessage requestMessage) throws JsonProcessingException {
        UUID id = UUID.randomUUID();
        persistRequestWithId(requestMessage, id);
        return id;
    }

    private void persistRequestWithId(ServiceRequestMessage requestMessage, UUID id) throws JsonProcessingException {
        boolean retry;
        do {
            retry = false;
            try {
                serviceRequestRepository.save(
                        ServiceRequest.builder()
                                .id(id)
                                .request(objectMapper.writeValueAsString(requestMessage))
                                .localDateTime(LocalDateTime.now())
                                .requestType(requestMessage.getClass().getName())
                                .status(ServiceRequestMessage.STATUS.PENDING)
                                .retryCount(0)
                                .build()
                );
            } catch (DuplicateKeyException dke) {
                id = UUID.randomUUID();
                retry = true;
            }
        } while (retry);

    }

    public void completeServiceRequest(ServiceRequestResponse serviceRequestResponse) {
        Optional<ServiceRequest> srQ = serviceRequestRepository.findById(serviceRequestResponse.getId());
        if (srQ.isPresent()) {
            ServiceRequest sr = srQ.get();
            sr.setStatus(serviceRequestResponse.getStatus());
            sr.setStatusMessage(serviceRequestResponse.getStatusMessage());
            serviceRequestRepository.save(sr);
        } else {
            log.error("void completeServiceRequest(ServiceRequestResponse serviceRequestResponse) not found: {}", serviceRequestResponse);
        }
    }

    public void statementComplete(StatementCompleteResponse statementCompleteResponse) {
        completeServiceRequest(statementCompleteResponse);
        redisComponent.removeLoanToCycle(statementCompleteResponse.getStatementDate(), statementCompleteResponse.getLoanId());
    }
}
