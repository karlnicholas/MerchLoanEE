package com.github.karlnicholas.merchloan.servicerequest.service;

import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Slf4j
public class ServiceRequestService {
//    private final MQProducers mqProducers;
//    private final ServiceRequestDao serviceRequestDao;
//    private final ObjectMapper objectMapper;
//    private final RedisComponent redisComponent;
//    @Resource(lookup = "java:jboss/datasources/ServiceRequestDS")
//    private DataSource dataSource;
//
//    @Inject
//    public ServiceRequestService(MQProducers mqProducers, ServiceRequestDao serviceRequestDao, RedisComponent redisComponent) {
//        this.mqProducers = mqProducers;
//        this.serviceRequestDao = serviceRequestDao;
//        this.redisComponent = redisComponent;
//        this.objectMapper = new ObjectMapper().findAndRegisterModules()
//                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
//    }
//
//    public UUID accountRequest(ServiceRequestMessage serviceRequestMessage, Boolean retry, UUID existingId) throws ServiceRequestException {
//        try {
//            AccountRequest accountRequest = (AccountRequest) serviceRequestMessage;
//            UUID id = retry == Boolean.TRUE ? existingId : persistRequest(accountRequest);
//            mqProducers.accountCreateAccount(CreateAccount.builder()
//                    .id(id)
//                    .customer(accountRequest.getCustomer())
//                    .createDate(redisComponent.getBusinessDate())
//                    .retry(retry)
//                    .build());
//            return id;
//        } catch (SQLException | JsonProcessingException ex) {
//            throw new ServiceRequestException(ex);
//        }
//    }
//
//    public UUID fundingRequest(ServiceRequestMessage serviceRequestMessage, Boolean retry, UUID existingId) throws ServiceRequestException {
//        try {
//            FundingRequest fundingRequest = (FundingRequest) serviceRequestMessage;
//            UUID id = null;
//            id = retry == Boolean.TRUE ? existingId : persistRequest(fundingRequest);
//            mqProducers.accountFundLoan(FundLoan.builder()
//                    .id(id)
//                    .accountId(fundingRequest.getAccountId())
//                    .amount(fundingRequest.getAmount())
//                    .startDate(redisComponent.getBusinessDate())
//                    .description(fundingRequest.getDescription())
//                    .retry(retry)
//                    .build()
//            );
//            return id;
//        } catch (SQLException | JsonProcessingException ex) {
//            throw new ServiceRequestException(ex);
//        }
//    }
//
//    public UUID accountValidateCreditRequest(ServiceRequestMessage serviceRequestMessage, Boolean retry, UUID existingId) throws ServiceRequestException {
//        try {
//            CreditRequest creditRequest = (CreditRequest) serviceRequestMessage;
//            UUID id = retry == Boolean.TRUE ? existingId : persistRequest(creditRequest);
//            mqProducers.accountValidateCredit(CreditLoan.builder()
//                    .id(id)
//                    .loanId(creditRequest.getLoanId())
//                    .date(redisComponent.getBusinessDate())
//                    .amount(creditRequest.getAmount())
//                    .description(creditRequest.getDescription())
//                    .retry(retry)
//                    .build()
//            );
//            return id;
//        } catch (SQLException | JsonProcessingException ex) {
//            throw new ServiceRequestException(ex);
//        }
//    }
//
//    public UUID statementStatementRequest(ServiceRequestMessage serviceRequestMessage, Boolean retry, UUID existingId) throws ServiceRequestException {
//        try {
//            StatementRequest statementRequest = (StatementRequest) serviceRequestMessage;
//            UUID id = retry == Boolean.TRUE ? existingId : persistRequest(statementRequest);
//            mqProducers.statementStatement(StatementHeader.builder()
//                    .id(id)
//                    .loanId(statementRequest.getLoanId())
//                    .interestChargeId(UUID.randomUUID())
//                    .feeChargeId(UUID.randomUUID())
//                    .statementDate(statementRequest.getStatementDate())
//                    .startDate(statementRequest.getStartDate())
//                    .endDate(statementRequest.getEndDate())
//                    .retry(retry)
//                    .build()
//            );
//            return id;
//        } catch (SQLException | JsonProcessingException ex) {
//            throw new ServiceRequestException(ex);
//        }
//    }
//
//    public UUID closeRequest(ServiceRequestMessage serviceRequestMessage, Boolean retry, UUID existingId) throws ServiceRequestException {
//        try {
//            CloseRequest closeRequest = (CloseRequest) serviceRequestMessage;
//            UUID id = retry == Boolean.TRUE ? existingId : persistRequest(closeRequest);
//            mqProducers.accountCloseLoan(CloseLoan.builder()
//                    .id(id)
//                    .loanId(closeRequest.getLoanId())
//                    .interestChargeId(UUID.randomUUID())
//                    .paymentId(UUID.randomUUID())
//                    .date(redisComponent.getBusinessDate())
//                    .amount(closeRequest.getAmount())
//                    .description(closeRequest.getDescription())
//                    .retry(retry)
//                    .build()
//            );
//            return id;
//        } catch (SQLException | JsonProcessingException ex) {
//            throw new ServiceRequestException(ex);
//        }
//    }
//
//    public UUID accountValidateDebitRequest(ServiceRequestMessage serviceRequestMessage, Boolean retry, UUID existingId) throws ServiceRequestException {
//        try {
//            DebitRequest debitRequest = (DebitRequest) serviceRequestMessage;
//            UUID id = retry == Boolean.TRUE ? existingId : persistRequest(debitRequest);
//            mqProducers.accountValidateDebit(DebitLoan.builder()
//                    .id(id)
//                    .loanId(debitRequest.getLoanId())
//                    .date(redisComponent.getBusinessDate())
//                    .amount(debitRequest.getAmount())
//                    .description(debitRequest.getDescription())
//                    .retry(retry)
//                    .build()
//            );
//            return id;
//        } catch (SQLException | JsonProcessingException ex) {
//            throw new ServiceRequestException(ex);
//        }
//    }
//
//    private UUID persistRequest(ServiceRequestMessage requestMessage) throws SQLException, JsonProcessingException {
//        UUID id = UUID.randomUUID();
//        persistRequestWithId(requestMessage, id);
//        return id;
//    }
//
//    private void persistRequestWithId(ServiceRequestMessage requestMessage, UUID id) throws JsonProcessingException, SQLException {
//        try (Connection con = dataSource.getConnection()) {
//            int retry = 0;
//            do {
//                try {
//                    serviceRequestDao.insert(con,
//                            ServiceRequestInit.builder()
//                                    .id(id)
//                                    .request(objectMapper.writeValueAsString(requestMessage))
//                                    .localDateTime(LocalDateTime.now())
//                                    .requestType(requestMessage.getClass().getName())
//                                    .status(ServiceRequestMessage.STATUS.PENDING)
//                                    .retryCount(0)
//                                    .build()
//                    );
//                } catch (SQLException ex) {
//                    if (ex.getErrorCode() == SqlUtils.DUPLICATE_ERROR && retry < 3) {
//                        retry++;
//                    } else {
//                        throw ex;
//                    }
//                    log.error("createAccount {}", ex);
//                    id = UUID.randomUUID();
//                }
//            } while (retry < 3);
//        }
//
//    }
//
//    public void completeServiceRequest(ServiceRequestResponse serviceRequestResponse) throws SQLException {
//        try (Connection con = dataSource.getConnection()) {
//            Optional<ServiceRequestInit> srQ = serviceRequestDao.findById(con, serviceRequestResponse.getId());
//            if (srQ.isPresent()) {
//                ServiceRequestInit sr = srQ.get();
//                sr.setStatus(serviceRequestResponse.getStatus());
//                sr.setStatusMessage(serviceRequestResponse.getStatusMessage());
//                serviceRequestDao.update(con, sr);
//            } else {
//                log.error("void completeServiceRequest(ServiceRequestResponseListener serviceRequestResponse) not found: {}", serviceRequestResponse);
//            }
//        }
//    }
//
//    public void statementComplete(StatementCompleteResponse statementCompleteResponse) throws SQLException {
//        completeServiceRequest(statementCompleteResponse);
//    }
//
}
