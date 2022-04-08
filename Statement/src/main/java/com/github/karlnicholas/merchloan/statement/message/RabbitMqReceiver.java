package com.github.karlnicholas.merchloan.statement.message;

import com.github.karlnicholas.merchloan.apimessage.message.ServiceRequestMessage;
import com.github.karlnicholas.merchloan.jmsmessage.*;
import com.github.karlnicholas.merchloan.statement.model.Statement;
import com.github.karlnicholas.merchloan.statement.service.QueryService;
import com.github.karlnicholas.merchloan.statement.service.StatementService;
import com.rabbitmq.client.Delivery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Component
@Slf4j
public class RabbitMqReceiver {
    private final StatementService statementService;
    private final RabbitMqSender rabbitMqSender;
    private final BigDecimal interestRate = new BigDecimal("0.10");
    private final BigDecimal interestMonths = new BigDecimal("12");

    public RabbitMqReceiver(StatementService statementService, QueryService queryService, RabbitMqSender rabbitMqSender) {
        this.statementService = statementService;
        this.rabbitMqSender = rabbitMqSender;
//        this.objectMapper = new ObjectMapper().findAndRegisterModules()
//                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    public void receivedStatementMessage(String consumerTag, Delivery delivery) throws IOException {
        StatementHeader statementHeader = (StatementHeader) SerializationUtils.deserialize(delivery.getBody());
        StatementCompleteResponse requestResponse = StatementCompleteResponse.builder()
                .id(statementHeader.getId())
                .statementDate(statementHeader.getStatementDate())
                .loanId(statementHeader.getLoanId())
                .build();
        try {
            log.debug("receivedStatementMessage {}", statementHeader);
            statementHeader = (StatementHeader) rabbitMqSender.accountQueryStatementHeader(statementHeader);
            if (statementHeader.getCustomer() != null) {
                Optional<Statement> statementExistsOpt = statementService.findStatement(statementHeader.getLoanId(), statementHeader.getStatementDate());
                if (statementExistsOpt.isEmpty()) {
                    Optional<Statement> lastStatement = statementService.findLastStatement(statementHeader.getLoanId());
                    // determine interest balance
                    BigDecimal interestBalance;
                    if (lastStatement.isPresent()) {
                        interestBalance = lastStatement.get().getEndingBalance();
                    } else if (!statementHeader.getRegisterEntries().isEmpty()) {
                        //TODO: assuming this is the funding entry. Horrible logic.
                        interestBalance = statementHeader.getRegisterEntries().get(0).getDebit();
                    } else {
                        //TODO: really should never get here.
                        interestBalance = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);
                    }
                    boolean paymentCreditFound = statementHeader.getRegisterEntries().stream().anyMatch(re -> re.getCredit() != null);
                    // so, let's do interest and fee calculations here.
                    if (!paymentCreditFound) {
                        RegisterEntryMessage feeRegisterEntry = (RegisterEntryMessage) rabbitMqSender.accountBillingCycleCharge(BillingCycleCharge.builder()
                                .id(statementHeader.getFeeChargeId())
                                .loanId(statementHeader.getLoanId())
                                .date(statementHeader.getStatementDate())
                                .debit(new BigDecimal("30.00"))
                                .description("Non payment fee")
                                .retry(statementHeader.getRetry())
                                .build()
                        );
                        statementHeader.getRegisterEntries().add(feeRegisterEntry);
                    }
                    BigDecimal interestAmt = interestBalance.multiply(interestRate).divide(interestMonths, 2, RoundingMode.HALF_EVEN);
                    RegisterEntryMessage interestRegisterEntry = (RegisterEntryMessage) rabbitMqSender.accountBillingCycleCharge(BillingCycleCharge.builder()
                            .id(statementHeader.getInterestChargeId())
                            .loanId(statementHeader.getLoanId())
                            .date(statementHeader.getStatementDate())
                            .debit(interestAmt)
                            .description("Interest")
                            .retry(statementHeader.getRetry())
                            .build()
                    );
                    statementHeader.getRegisterEntries().add(interestRegisterEntry);
                    BigDecimal startingBalance = lastStatement.isPresent() ? lastStatement.get().getEndingBalance() : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);
                    BigDecimal endingBalance = startingBalance;
                    for (RegisterEntryMessage re : statementHeader.getRegisterEntries()) {
                        if (re.getCredit() != null) {
                            endingBalance = endingBalance.subtract(re.getCredit());
                            re.setBalance(endingBalance);
                        }
                        if (re.getDebit() != null) {
                            endingBalance = endingBalance.add(re.getDebit());
                            re.setBalance(endingBalance);
                        }
                    }
                    // so, done with interest and fee calculations here?
                    statementService.saveStatement(statementHeader, startingBalance, endingBalance);
                    if (endingBalance.compareTo(BigDecimal.ZERO) <= 0) {
                        requestResponse.setSuccess("Loan Closed");
                        rabbitMqSender.accountLoanClosed(statementHeader);
                    } else {
                        requestResponse.setSuccess("Statement created");
                    }
                } else {
                    requestResponse.setFailure("WARN: Statement already exists for loanId " + statementHeader.getLoanId() + " and statement date " + statementHeader.getStatementDate());
                }
            } else {
                requestResponse.setFailure("ERROR: Account/Loan not found for accountId " + statementHeader.getAccountId() + " and loanId " + statementHeader.getLoanId());
            }
        } catch (Exception ex) {
            log.error("void receivedServiceRequestMessage(ServiceRequestResponse serviceRequest) exception {}", ex.getMessage());
            requestResponse.setError(ex.getMessage());
        } finally {
            //TODO: Sloppy
            if (!requestResponse.getStatusMessage().equalsIgnoreCase("Loan Closed")) {
                rabbitMqSender.serviceRequestStatementComplete(requestResponse);
            }
        }
    }

//
//    public void receivedStatementMessage(String consumerTag, Delivery delivery) throws IOException {
//        StatementHeader statementHeader = (StatementHeader) SerializationUtils.deserialize(delivery.getBody());
//        StatementCompleteResponse requestResponse = StatementCompleteResponse.builder()
//                .id(statementHeader.getId())
//                .statementDate(statementHeader.getStatementDate())
//                .loanId(statementHeader.getLoanId())
//                .build();
//        try {
//            log.debug("receivedStatementMessage {}", statementHeader);
//            statementHeader = (StatementHeader) rabbitMqSender.accountQueryStatementHeader(statementHeader);
//            if (statementHeader.getCustomer() != null) {
//                boolean paymentCreditFound = statementHeader.getRegisterEntries().stream().anyMatch(re -> re.getCredit() != null);
//                // so, let's do interest and fee calculations here.
//                if (!paymentCreditFound) {
//                    RegisterEntryMessage feeRegisterEntry = (RegisterEntryMessage) rabbitMqSender.accountBillingCycleCharge(BillingCycleCharge.builder()
//                            .id(statementHeader.getFeeChargeId())
//                            .loanId(statementHeader.getLoanId())
//                            .date(statementHeader.getStatementDate())
//                            .debit(new BigDecimal("30.00"))
//                            .description("Non payment fee")
//                            .retry(statementHeader.getRetry())
//                            .build()
//                    );
//                    statementHeader.getRegisterEntries().add(feeRegisterEntry);
//                }
//                Optional<Statement> statementExistsOpt = statementService.findStatement(statementHeader.getLoanId(), statementHeader.getStatementDate());
//                if (statementExistsOpt.isEmpty()) {
//                    Optional<Statement> lastStatement = statementService.findLastStatement(statementHeader.getLoanId());
//                    // determine interest balance
//                    BigDecimal interestBalance;
//                    if (lastStatement.isPresent()) {
//                        interestBalance = lastStatement.get().getEndingBalance();
//                    } else if (!statementHeader.getRegisterEntries().isEmpty()) {
//                        //TODO: assuming this is the funding entry. Horrible logic.
//                        interestBalance = statementHeader.getRegisterEntries().get(0).getDebit();
//                    } else {
//                        //TODO: really should never get here.
//                        interestBalance = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);
//                    }
//                    BigDecimal interestAmt = interestBalance.multiply(interestRate).divide(interestMonths, 2, RoundingMode.HALF_EVEN);
//                    RegisterEntryMessage interestRegisterEntry = (RegisterEntryMessage) rabbitMqSender.accountBillingCycleCharge(BillingCycleCharge.builder()
//                            .id(statementHeader.getInterestChargeId())
//                            .loanId(statementHeader.getLoanId())
//                            .date(statementHeader.getStatementDate())
//                            .debit(interestAmt)
//                            .description("Interest")
//                            .retry(statementHeader.getRetry())
//                            .build()
//                    );
//                    statementHeader.getRegisterEntries().add(interestRegisterEntry);
//                    BigDecimal startingBalance = lastStatement.isPresent() ? lastStatement.get().getEndingBalance() : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);
//                    BigDecimal endingBalance = startingBalance;
//                    for (RegisterEntryMessage re : statementHeader.getRegisterEntries()) {
//                        if (re.getCredit() != null) {
//                            endingBalance = endingBalance.subtract(re.getCredit());
//                            re.setBalance(endingBalance);
//                        }
//                        if (re.getDebit() != null) {
//                            endingBalance = endingBalance.add(re.getDebit());
//                            re.setBalance(endingBalance);
//                        }
//                    }
//                    // so, done with interest and fee calculations here?
//                    statementService.saveStatement(statementHeader, startingBalance, endingBalance);
//                    if (endingBalance.compareTo(BigDecimal.ZERO) <= 0) {
//                        requestResponse.setSuccess("Loan Closed");
//                        rabbitMqSender.accountLoanClosed(statementHeader);
//                    } else {
//                        requestResponse.setSuccess("Statement created");
//                    }
//                } else {
//                    requestResponse.setFailure("WARN: Statement already exists for loanId " + statementHeader.getLoanId() + " and statement date " + statementHeader.getStatementDate());
//                }
//            } else {
//                requestResponse.setFailure("ERROR: Account/Loan not found for accountId " + statementHeader.getAccountId() + " and loanId " + statementHeader.getLoanId());
//            }
//        } catch (Exception ex) {
//            log.error("void receivedServiceRequestMessage(ServiceRequestResponse serviceRequest) exception {}", ex.getMessage());
//            requestResponse.setError(ex.getMessage());
//        } finally {
//            //TODO: Sloppy
//            if (!requestResponse.getStatusMessage().equalsIgnoreCase("Loan Closed")) {
//                rabbitMqSender.serviceRequestStatementComplete(requestResponse);
//            }
//        }
//    }
//
    public void receivedCloseStatementMessage(String consumerTag, Delivery delivery) {
        StatementHeader statementHeader = (StatementHeader) SerializationUtils.deserialize(delivery.getBody());
        try {
            log.debug("receivedCloseStatementMessage {}", statementHeader);
            Optional<Statement> statementExistsOpt = statementService.findStatement(statementHeader.getLoanId(), statementHeader.getStatementDate());
            if (statementExistsOpt.isEmpty()) {
                // determine interest balance
                Optional<Statement> lastStatement = statementService.findLastStatement(statementHeader.getLoanId());
                BigDecimal startingBalance = lastStatement.isPresent() ? lastStatement.get().getEndingBalance() : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);
                BigDecimal endingBalance = startingBalance;
                for (RegisterEntryMessage re : statementHeader.getRegisterEntries()) {
                    if (re.getCredit() != null) {
                        endingBalance = endingBalance.subtract(re.getCredit());
                        re.setBalance(endingBalance);
                    }
                    if (re.getDebit() != null) {
                        endingBalance = endingBalance.add(re.getDebit());
                        re.setBalance(endingBalance);
                    }
                }
                // so, done with interest and fee calculations here?
                statementService.saveStatement(statementHeader, startingBalance, endingBalance);
            }
            // just to save bandwidth
            statementHeader.setRegisterEntries(null);
            rabbitMqSender.accountLoanClosed(statementHeader);
        } catch (Exception ex) {
            log.error("void receivedCloseStatementMessage(StatementHeader statementHeader) exception {}", ex.getMessage());
            try {
                ServiceRequestResponse requestResponse = new ServiceRequestResponse(statementHeader.getId(), ServiceRequestMessage.STATUS.ERROR, ex.getMessage());
                rabbitMqSender.serviceRequestServiceRequest(requestResponse);
            } catch (Exception innerEx) {
                log.error("ERROR SENDING ERROR", ex);
            }
        }
    }
}