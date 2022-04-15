package com.github.karlnicholas.merchloan.statement.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.karlnicholas.merchloan.apimessage.message.ServiceRequestMessage;
import com.github.karlnicholas.merchloan.jms.config.MQQueueNames;
import com.github.karlnicholas.merchloan.jmsmessage.*;
import com.github.karlnicholas.merchloan.statement.model.Statement;
import com.github.karlnicholas.merchloan.statement.service.QueryService;
import com.github.karlnicholas.merchloan.statement.service.StatementService;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class MQConsumers {
    private final MQQueueNames mqQueueNames;
    private final QueryService queryService;
    private Channel responseChannel;
    private final ObjectMapper objectMapper;
    private final StatementService statementService;
    private final MQProducers mqProducers;
    private final BigDecimal interestRate = new BigDecimal("0.10");
    private final BigDecimal interestMonths = new BigDecimal("12");

    public MQConsumers(Connection connection, MQQueueNames mqQueueNames, MQProducers mqProducers, StatementService statementService, QueryService queryService) throws IOException {
        this.statementService = statementService;
        this.mqProducers = mqProducers;
        this.mqQueueNames = mqQueueNames;
        this.queryService = queryService;
        this.objectMapper = new ObjectMapper().findAndRegisterModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

//                .with(rabbitMqProperties.getStatementStatementRoutingkey())
        Channel statementStatementChannel = connection.createChannel();
        statementStatementChannel.exchangeDeclare(this.mqQueueNames.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        statementStatementChannel.queueDeclare(this.mqQueueNames.getStatementStatementQueue(), false, true, true, null);
        statementStatementChannel.queueBind(this.mqQueueNames.getStatementStatementQueue(), this.mqQueueNames.getExchange(), this.mqQueueNames.getStatementStatementQueue());
        statementStatementChannel.basicConsume(this.mqQueueNames.getStatementStatementQueue(), true, this::receivedStatementMessage, consumerTag -> {});

//                .with(rabbitMqProperties.getStatementCloseStatementRoutingkey())
        Channel statementCloseStatementChannel = connection.createChannel();
        statementCloseStatementChannel.exchangeDeclare(this.mqQueueNames.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        statementCloseStatementChannel.queueDeclare(this.mqQueueNames.getStatementCloseStatementQueue(), false, true, true, null);
        statementCloseStatementChannel.queueBind(this.mqQueueNames.getStatementCloseStatementQueue(), this.mqQueueNames.getExchange(), this.mqQueueNames.getStatementCloseStatementQueue());
        statementCloseStatementChannel.basicConsume(this.mqQueueNames.getStatementCloseStatementQueue(), true, this::receivedCloseStatementMessage, consumerTag -> {});
//                .with(rabbitMqProperties.getStatementQueryStatementRoutingkey())
        Channel statementQueryStatementChannel = connection.createChannel();
        statementQueryStatementChannel.exchangeDeclare(this.mqQueueNames.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        statementQueryStatementChannel.queueDeclare(this.mqQueueNames.getStatementQueryStatementQueue(), false, true, true, null);
        statementQueryStatementChannel.queueBind(this.mqQueueNames.getStatementQueryStatementQueue(), this.mqQueueNames.getExchange(), this.mqQueueNames.getStatementQueryStatementQueue());
        statementQueryStatementChannel.basicConsume(this.mqQueueNames.getStatementQueryStatementQueue(), true, this::receivedQueryStatementMessage, consumerTag -> {});
//                .with(rabbitMqProperties.getStatementQueryStatementsRoutingkey())
        Channel statementQueryStatementsChannel = connection.createChannel();
        statementQueryStatementsChannel.exchangeDeclare(this.mqQueueNames.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        statementQueryStatementsChannel.queueDeclare(this.mqQueueNames.getStatementQueryStatementsQueue(), false, true, true, null);
        statementQueryStatementsChannel.queueBind(this.mqQueueNames.getStatementQueryStatementsQueue(), this.mqQueueNames.getExchange(), this.mqQueueNames.getStatementQueryStatementsQueue());
        statementQueryStatementsChannel.basicConsume(this.mqQueueNames.getStatementQueryStatementsQueue(), true, this::receivedQueryStatementsMessage, consumerTag -> {});
//                .with(rabbitMqProperties.getStatementQueryMostRecentStatementRoutingkey())
        Channel statementQueryMostRecentStatementChannel = connection.createChannel();
        statementQueryMostRecentStatementChannel.exchangeDeclare(this.mqQueueNames.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        statementQueryMostRecentStatementChannel.queueDeclare(this.mqQueueNames.getStatementQueryMostRecentStatementQueue(), false, true, true, null);
        statementQueryMostRecentStatementChannel.queueBind(this.mqQueueNames.getStatementQueryMostRecentStatementQueue(), this.mqQueueNames.getExchange(), this.mqQueueNames.getStatementQueryMostRecentStatementQueue());
        statementQueryMostRecentStatementChannel.basicConsume(this.mqQueueNames.getStatementQueryMostRecentStatementQueue(), true, this::receivedQueryMostRecentStatementMessage, consumerTag -> {log.error("CANCEL????");});

        responseChannel = connection.createChannel();
    }

    public void receivedQueryStatementMessage(String consumerTag, Delivery delivery) throws IOException {
        UUID loanId = (UUID) SerializationUtils.deserialize(delivery.getBody());
        log.debug("receivedQueryStatementMessage {}", loanId);
        String result = queryService.findById(loanId).map(Statement::getStatement).orElse("ERROR: No statement found for id " + loanId);
        reply(delivery, result);
    }

    public void receivedQueryMostRecentStatementMessage(String consumerTag, Delivery delivery) throws IOException {
        UUID loanId = (UUID) SerializationUtils.deserialize(delivery.getBody());
        log.debug("receivedQueryMostRecentStatementMessage {}", loanId);
        try {
            MostRecentStatement mostRecentStatement = queryService.findMostRecentStatement(loanId).map(statement -> MostRecentStatement.builder()
                            .id(statement.getId())
                            .loanId(loanId)
                            .statementDate(statement.getStatementDate())
                            .endingBalance(statement.getEndingBalance())
                            .startingBalance(statement.getStartingBalance())
                            .build())
                    .orElse(MostRecentStatement.builder().loanId(loanId).build());
            reply(delivery, mostRecentStatement);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void receivedQueryStatementsMessage(String consumerTag, Delivery delivery) throws IOException {
        UUID id = (UUID) SerializationUtils.deserialize(delivery.getBody());
        log.debug("receivedQueryStatementsMessage Received {}", id);
        reply(delivery, objectMapper.writeValueAsString(queryService.findByLoanId(id)));
    }

    private void reply(Delivery delivery, Object data) throws IOException {
        AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                .Builder()
                .correlationId(delivery.getProperties().getCorrelationId())
                .build();
        responseChannel.basicPublish(mqQueueNames.getExchange(), delivery.getProperties().getReplyTo(), replyProps, SerializationUtils.serialize(data));
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
            statementHeader = (StatementHeader) mqProducers.accountQueryStatementHeader(statementHeader);
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
                        RegisterEntryMessage feeRegisterEntry = (RegisterEntryMessage) mqProducers.accountBillingCycleCharge(BillingCycleCharge.builder()
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
                    RegisterEntryMessage interestRegisterEntry = (RegisterEntryMessage) mqProducers.accountBillingCycleCharge(BillingCycleCharge.builder()
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
                        mqProducers.accountLoanClosed(statementHeader);
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
                mqProducers.serviceRequestStatementComplete(requestResponse);
            }
        }
    }

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
            mqProducers.accountLoanClosed(statementHeader);
        } catch (Exception ex) {
            log.error("void receivedCloseStatementMessage(StatementHeader statementHeader) exception {}", ex.getMessage());
            try {
                ServiceRequestResponse requestResponse = new ServiceRequestResponse(statementHeader.getId(), ServiceRequestMessage.STATUS.ERROR, ex.getMessage());
                mqProducers.serviceRequestServiceRequest(requestResponse);
            } catch (Exception innerEx) {
                log.error("ERROR SENDING ERROR", ex);
            }
        }
    }
}