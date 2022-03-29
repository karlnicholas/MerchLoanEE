package com.github.karlnicholas.merchloan.statement.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.karlnicholas.merchloan.apimessage.message.ServiceRequestMessage;
import com.github.karlnicholas.merchloan.jms.message.RabbitMqSender;
import com.github.karlnicholas.merchloan.jmsmessage.*;
import com.github.karlnicholas.merchloan.statement.model.Statement;
import com.github.karlnicholas.merchloan.statement.service.QueryService;
import com.github.karlnicholas.merchloan.statement.service.StatementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class RabbitMqReceiver implements RabbitListenerConfigurer {
    private final StatementService statementService;
    private final QueryService queryService;
    private final RabbitMqSender rabbitMqSender;
    private final ObjectMapper objectMapper;
    private final BigDecimal interestRate = new BigDecimal("0.10");
    private final BigDecimal interestMonths = new BigDecimal("12");

    public RabbitMqReceiver(StatementService statementService, QueryService queryService, RabbitMqSender rabbitMqSender) {
        this.statementService = statementService;
        this.queryService = queryService;
        this.rabbitMqSender = rabbitMqSender;
        this.objectMapper = new ObjectMapper().findAndRegisterModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar rabbitListenerEndpointRegistrar) {
        // no configuration needed
    }

    @RabbitListener(queues = "${rabbitmq.statement.statement.queue}", returnExceptions = "true")
    public void receivedStatementMessage(StatementHeader statementHeader) {
        StatementCompleteResponse requestResponse = StatementCompleteResponse.builder()
                .id(statementHeader.getId())
                .statementDate(statementHeader.getStatementDate())
                .loanId(statementHeader.getLoanId())
                .build();
        try {
            log.debug("receivedStatementMessage {}", statementHeader);
            statementHeader = (StatementHeader) rabbitMqSender.accountQueryStatementHeader(statementHeader);
            if (statementHeader.getCustomer() != null) {
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
            throw new AmqpRejectAndDontRequeueException(ex);
        } finally {
            //TODO: Sloppy
            if (!requestResponse.getStatusMessage().equalsIgnoreCase("Loan Closed")) {
                rabbitMqSender.serviceRequestStatementComplete(requestResponse);
            }
        }
    }

    @RabbitListener(queues = "${rabbitmq.statement.closestatement.queue}", returnExceptions = "true")
    public void receivedCloseStatementMessage(StatementHeader statementHeader) {
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
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }

    @RabbitListener(queues = "${rabbitmq.statement.query.statement.queue}", returnExceptions = "true")
    public String receivedQueryStatementMessage(UUID loanId) {
        try {
            log.debug("receivedQueryStatementMessage {}", loanId);
            return queryService.findById(loanId).map(Statement::getStatement).orElse("ERROR: No statement found for id " + loanId);
        } catch (Exception ex) {
            log.error("String receivedQueryStatementMessage(UUID id) exception {}", ex.getMessage());
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }

    @RabbitListener(queues = "${rabbitmq.statement.query.mostrecentstatement.queue}", returnExceptions = "true")
    public MostRecentStatement receivedQueryMostRecentStatementMessage(UUID loanId) {
        try {
            log.debug("receivedQueryMostRecentStatementMessage {}", loanId);
            return queryService.findMostRecentStatement(loanId).map(statement -> MostRecentStatement.builder()
                    .id(statement.getId())
                    .loanId(loanId)
                    .statementDate(statement.getStatementDate())
                    .endingBalance(statement.getEndingBalance())
                    .startingBalance(statement.getStartingBalance())
                    .build()).orElse(MostRecentStatement.builder().loanId(loanId).build());
        } catch (Exception ex) {
            log.error("String receivedQueryMostRecentStatementMessage(UUID id) exception {}", ex.getMessage());
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }

    @RabbitListener(queues = "${rabbitmq.statement.query.statements.queue}", returnExceptions = "true")
    public String receivedQueryStatementsMessage(UUID id) {
        try {
            log.debug("receivedQueryStatementsMessage Received {}", id);
            return objectMapper.writeValueAsString(queryService.findByLoanId(id));
        } catch (Exception ex) {
            log.error("String receivedQueryStatementsMessage(UUID id) exception {}", ex.getMessage());
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }

}