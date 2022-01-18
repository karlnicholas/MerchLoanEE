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

import java.time.LocalDate;
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

    public UUID accountRequest(AccountRequest accountRequest) throws JsonProcessingException {
        UUID id = persistRequest(accountRequest);
        try {
            rabbitMqSender.accountCreateAccount(CreateAccount.builder()
                    .id(id)
                    .customer(accountRequest.getCustomer())
                    .createDate(redisComponent.getBusinessDate())
                    .retryCount(0)
                    .build());
        } catch (Exception e) {
            log.error("Send account create message failed: {}", e.getMessage());
        }
        return id;
    }

    public UUID fundingRequest(FundingRequest fundingRequest) throws JsonProcessingException {
        UUID id = persistRequest(fundingRequest);
        rabbitMqSender.accountFundLoan(
                FundLoan.builder()
                        .id(id)
                        .accountId(fundingRequest.getAccountId())
                        .amount(fundingRequest.getAmount())
                        .startDate(redisComponent.getBusinessDate())
                        .description(fundingRequest.getDescription())
                        .retryCount(0)
                        .build()
        );
        return id;
    }

    public UUID accountValidateCreditRequest(CreditRequest creditRequest) throws JsonProcessingException {
        UUID id = persistRequest(creditRequest);
        rabbitMqSender.accountValidateCredit(
                CreditLoan.builder()
                        .id(id)
                        .loanId(creditRequest.getLoanId())
                        .date(redisComponent.getBusinessDate())
                        .amount(creditRequest.getAmount())
                        .description(creditRequest.getDescription())
                        .retryCount(0)
                        .build()
        );
        return id;
    }

    public UUID statementStatementRequest(StatementRequest statementRequest) throws JsonProcessingException {
        UUID id = persistRequest(statementRequest);
        rabbitMqSender.statementStatement(
                StatementHeader.builder()
                        .id(id)
                        .accountId(statementRequest.getAccountId())
                        .loanId(statementRequest.getLoanId())
                        .statementDate(statementRequest.getStatementDate())
                        .startDate(statementRequest.getStartDate())
                        .endDate(statementRequest.getEndDate())
                        .retryCount(0)
                        .build()
        );
        return id;
    }

    public UUID accountValidateDebitRequest(DebitRequest debitRequest) throws JsonProcessingException {
        UUID id = persistRequest(debitRequest);
        rabbitMqSender.accountValidateDebit(
                DebitLoan.builder()
                        .loanId(debitRequest.getLoanId())
                        .date(redisComponent.getBusinessDate())
                        .amount(debitRequest.getAmount())
                        .description(debitRequest.getDescription())
                        .build()
        );
        return id;
    }

    private UUID persistRequest(ServiceRequestMessage requestMessage) throws JsonProcessingException {
        UUID id = UUID.randomUUID();
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
                                .status(ServiceRequestResponse.STATUS.PENDING)
                                .build()
                );
            } catch (DuplicateKeyException dke) {
                id = UUID.randomUUID();
                retry = true;
            }
        } while (retry);
        return id;
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

    public Boolean checkRequest(LocalDate businessDate) {
        return serviceRequestRepository.existsBylocalDateTimeLessThanAndStatusEquals(businessDate.plusDays(1).atStartOfDay(), ServiceRequestResponse.STATUS.PENDING);
    }
}
