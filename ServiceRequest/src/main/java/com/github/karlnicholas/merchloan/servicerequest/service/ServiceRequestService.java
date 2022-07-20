package com.github.karlnicholas.merchloan.servicerequest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.karlnicholas.merchloan.apimessage.message.*;
import com.github.karlnicholas.merchloan.jmsmessage.*;
import com.github.karlnicholas.merchloan.redis.component.RedisComponent;
import com.github.karlnicholas.merchloan.servicerequest.component.ServiceRequestException;
import com.github.karlnicholas.merchloan.servicerequest.dao.ServiceRequestDao;
import com.github.karlnicholas.merchloan.servicerequest.model.ServiceRequest;
import com.github.karlnicholas.merchloan.sqlutil.SqlUtils;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.*;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
@Slf4j
public class ServiceRequestService {
    @Inject
    private JMSContext jmsContext;
    @Resource(lookup = "java:global/jms/queue/AccountCreateAccountQueue")
    private Queue accountCreateAccountQueue;
    @Resource(lookup = "java:global/jms/queue/AccountFundingQueue")
    private Queue accountFundingQueue;
    @Resource(lookup = "java:global/jms/queue/AccountValidateCreditQueue")
    private Queue accountValidateCreditQueue;
    @Resource(lookup = "java:global/jms/queue/AccountValidateDebitQueue")
    private Queue accountValidateDebitQueue;
    @Resource(lookup = "java:global/jms/queue/StatementStatementQueue")
    private Queue statementStatementQueue;
    @Resource(lookup = "java:global/jms/queue/AccountCloseLoanQueue")
    private Queue accountCloseLoanQueue;
    private JMSProducer accountCloseLoanProducer;    @Inject
    private ServiceRequestDao serviceRequestDao;
    private ObjectMapper objectMapper;
    @Inject
    private RedisComponent redisComponent;
    @Resource(lookup = "java:jboss/datasources/ServiceRequestDS")
    private DataSource dataSource;

    public ServiceRequestService() {
        this.objectMapper = new ObjectMapper().findAndRegisterModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    public UUID accountRequest(ServiceRequestMessage serviceRequestMessage, Boolean retry, UUID existingId) throws ServiceRequestException {
        try {
            AccountRequest accountRequest = (AccountRequest) serviceRequestMessage;
            UUID id = retry == Boolean.TRUE ? existingId : persistRequest(accountRequest);
            jmsContext.createProducer().setDeliveryMode(DeliveryMode.NON_PERSISTENT).send(accountCreateAccountQueue, jmsContext.createObjectMessage(
            CreateAccount.builder()
                    .id(id)
                    .customer(accountRequest.getCustomer())
                    .createDate(redisComponent.getBusinessDate())
                    .retry(retry)
                    .build()));
            return id;
        } catch (SQLException | JsonProcessingException ex) {
            throw new ServiceRequestException(ex);
        }
    }

    public UUID fundingRequest(ServiceRequestMessage serviceRequestMessage, Boolean retry, UUID existingId) throws ServiceRequestException {
        try {
            FundingRequest fundingRequest = (FundingRequest) serviceRequestMessage;
            UUID id = null;
            id = retry == Boolean.TRUE ? existingId : persistRequest(fundingRequest);
            jmsContext.createProducer().setDeliveryMode(DeliveryMode.NON_PERSISTENT).send(accountFundingQueue, jmsContext.createObjectMessage(FundLoan.builder()
                    .id(id)
                    .accountId(fundingRequest.getAccountId())
                    .amount(fundingRequest.getAmount())
                    .startDate(redisComponent.getBusinessDate())
                    .description(fundingRequest.getDescription())
                    .retry(retry)
                    .build()));
            return id;
        } catch (SQLException | JsonProcessingException ex) {
            throw new ServiceRequestException(ex);
        }
    }

    public UUID accountValidateCreditRequest(ServiceRequestMessage serviceRequestMessage, Boolean retry, UUID existingId) throws ServiceRequestException {
        try {
            CreditRequest creditRequest = (CreditRequest) serviceRequestMessage;
            UUID id = retry == Boolean.TRUE ? existingId : persistRequest(creditRequest);
            jmsContext.createProducer().setDeliveryMode(DeliveryMode.NON_PERSISTENT).send(accountValidateCreditQueue, jmsContext.createObjectMessage(CreditLoan.builder()
                    .id(id)
                    .loanId(creditRequest.getLoanId())
                    .date(redisComponent.getBusinessDate())
                    .amount(creditRequest.getAmount())
                    .description(creditRequest.getDescription())
                    .retry(retry)
                    .build()));
            return id;
        } catch (SQLException | JsonProcessingException ex) {
            throw new ServiceRequestException(ex);
        }
    }

    public UUID statementStatementRequest(ServiceRequestMessage serviceRequestMessage, Boolean retry, UUID existingId) throws ServiceRequestException {
        try {
            StatementRequest statementRequest = (StatementRequest) serviceRequestMessage;
            UUID id = retry == Boolean.TRUE ? existingId : persistRequest(statementRequest);
            jmsContext.createProducer().setDeliveryMode(DeliveryMode.NON_PERSISTENT).send(statementStatementQueue, jmsContext.createObjectMessage(StatementHeader.builder()
                    .id(id)
                    .loanId(statementRequest.getLoanId())
                    .interestChargeId(UUID.randomUUID())
                    .feeChargeId(UUID.randomUUID())
                    .statementDate(statementRequest.getStatementDate())
                    .startDate(statementRequest.getStartDate())
                    .endDate(statementRequest.getEndDate())
                    .retry(retry)
                    .build()));
            return id;
        } catch (SQLException | JsonProcessingException ex) {
            throw new ServiceRequestException(ex);
        }
    }

    public UUID closeRequest(ServiceRequestMessage serviceRequestMessage, Boolean retry, UUID existingId) throws ServiceRequestException {
        try {
            CloseRequest closeRequest = (CloseRequest) serviceRequestMessage;
            UUID id = retry == Boolean.TRUE ? existingId : persistRequest(closeRequest);
            accountCloseLoanProducer.send(accountCloseLoanQueue, jmsContext.createObjectMessage(CloseLoan.builder()
                    .id(id)
                    .loanId(closeRequest.getLoanId())
                    .interestChargeId(UUID.randomUUID())
                    .paymentId(UUID.randomUUID())
                    .date(redisComponent.getBusinessDate())
                    .amount(closeRequest.getAmount())
                    .description(closeRequest.getDescription())
                    .retry(retry)
                    .build()));
            return id;
        } catch (SQLException | JsonProcessingException ex) {
            throw new ServiceRequestException(ex);
        }
    }

    public UUID accountValidateDebitRequest(ServiceRequestMessage serviceRequestMessage, Boolean retry, UUID existingId) throws ServiceRequestException {
        try {
            DebitRequest debitRequest = (DebitRequest) serviceRequestMessage;
            UUID id = retry == Boolean.TRUE ? existingId : persistRequest(debitRequest);
            jmsContext.createProducer().setDeliveryMode(DeliveryMode.NON_PERSISTENT).send(accountValidateDebitQueue, jmsContext.createObjectMessage(DebitLoan.builder()
                    .id(id)
                    .loanId(debitRequest.getLoanId())
                    .date(redisComponent.getBusinessDate())
                    .amount(debitRequest.getAmount())
                    .description(debitRequest.getDescription())
                    .retry(retry)
                    .build()));
            return id;
        } catch (SQLException | JsonProcessingException ex) {
            throw new ServiceRequestException(ex);
        }
    }

    private UUID persistRequest(ServiceRequestMessage requestMessage) throws SQLException, JsonProcessingException {
        UUID id = UUID.randomUUID();
        persistRequestWithId(requestMessage, id);
        return id;
    }

    private void persistRequestWithId(ServiceRequestMessage requestMessage, UUID id) throws JsonProcessingException, SQLException {
        try (Connection con = dataSource.getConnection()) {
            boolean retry;
            do {
                retry = false;
                try {
                    serviceRequestDao.insert(con,
                            ServiceRequest.builder()
                                    .id(id)
                                    .request(objectMapper.writeValueAsString(requestMessage))
                                    .localDateTime(LocalDateTime.now())
                                    .requestType(requestMessage.getClass().getName())
                                    .status(ServiceRequestMessage.STATUS.PENDING)
                                    .retryCount(0)
                                    .build()
                    );
                } catch (SQLException ex) {
                    if (ex.getErrorCode() == SqlUtils.DUPLICATE_ERROR ) {
                        id = UUID.randomUUID();
                        retry = true;
                    }
                }
            } while (retry);
        }
    }

    public void completeServiceRequest(ServiceRequestResponse serviceRequestResponse) throws SQLException {
        try (Connection con = dataSource.getConnection()) {
            Optional<ServiceRequest> srQ = serviceRequestDao.findById(con, serviceRequestResponse.getId());
            if (srQ.isPresent()) {
                ServiceRequest sr = srQ.get();
                sr.setStatus(serviceRequestResponse.getStatus());
                sr.setStatusMessage(serviceRequestResponse.getStatusMessage());
                serviceRequestDao.update(con, sr);
            } else {
                log.error("void completeServiceRequest(ServiceRequestResponseListener serviceRequestResponse) not found: {}", serviceRequestResponse.getId());
            }
        }
    }

    public void statementComplete(StatementCompleteResponse statementCompleteResponse) throws SQLException {
//        try (Connection con = dataSource.getConnection()) {
//            log.info("{} srs {}", statementCompleteResponse.getId(), serviceRequestDao.findAll(con).stream().map(sr->sr.getId()).collect(Collectors.toList()));
//        }
        completeServiceRequest(statementCompleteResponse);
    }

}
