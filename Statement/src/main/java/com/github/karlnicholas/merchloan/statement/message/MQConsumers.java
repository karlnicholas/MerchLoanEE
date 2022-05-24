package com.github.karlnicholas.merchloan.statement.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.karlnicholas.merchloan.apimessage.message.ServiceRequestMessage;
import com.github.karlnicholas.merchloan.jms.MQConsumerUtils;
import com.github.karlnicholas.merchloan.jmsmessage.*;
import com.github.karlnicholas.merchloan.statement.model.Statement;
import com.github.karlnicholas.merchloan.statement.service.QueryService;
import com.github.karlnicholas.merchloan.statement.service.StatementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.jms.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class MQConsumers {
    private final Session session;
    private final StatementService statementService;
    private final MQProducers mqProducers;
    private final QueryService queryService;
    private final ObjectMapper objectMapper;
    private static final String LOG_STRING = "onStatementMessage";
    private final BigDecimal interestRate = new BigDecimal("0.10");
    private final BigDecimal interestMonths = new BigDecimal("12");

    public MQConsumers(Connection connection, MQConsumerUtils mqConsumerUtils, StatementService statementService, MQProducers mqProducers, QueryService queryService) throws JMSException {
//        this.connection = connection;
        session = connection.createSession();
        this.statementService = statementService;
        this.mqProducers = mqProducers;
        this.queryService = queryService;

        objectMapper = new ObjectMapper().findAndRegisterModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        mqConsumerUtils.bindConsumer(session, session.createQueue(mqConsumerUtils.getStatementStatementQueue()), this::onStatementMessage);
        mqConsumerUtils.bindConsumer(session, session.createQueue(mqConsumerUtils.getStatementCloseStatementQueue()), this::onCloseStatementMessage);
        mqConsumerUtils.bindConsumer(session, session.createQueue(mqConsumerUtils.getStatementQueryStatementQueue()), this::onQueryStatementMessage);
        mqConsumerUtils.bindConsumer(session, session.createQueue(mqConsumerUtils.getStatementQueryStatementsQueue()), this::onQueryStatementsMessage);
        mqConsumerUtils.bindConsumer(session, session.createQueue(mqConsumerUtils.getStatementQueryMostRecentStatementQueue()), this::onQueryMostRecentStatementMessage);

        connection.start();
    }

    public void onStatementMessage(Message message) {
        StatementHeader statementHeader = null;
        try {
            statementHeader = (StatementHeader) ((ObjectMessage) message).getObject();
        } catch (JMSException e) {
            log.error(LOG_STRING, e);
        }
        StatementCompleteResponse requestResponse = StatementCompleteResponse.builder().id(statementHeader.getId()).statementDate(statementHeader.getStatementDate()).loanId(statementHeader.getLoanId()).build();
        boolean loanClosed = false;
        try {
            log.debug("onStatementMessage {}", statementHeader);
            statementHeader = (StatementHeader) mqProducers.accountQueryStatementHeader(session, statementHeader);
            if (statementHeader.getCustomer() == null) {
                requestResponse.setFailure("ERROR: Account/Loan not found for accountId " + statementHeader.getAccountId() + " and loanId " + statementHeader.getLoanId());
                return;
            }
            Optional<Statement> statementExistsOpt = statementService.findStatement(statementHeader.getLoanId(), statementHeader.getStatementDate());
            if (statementExistsOpt.isPresent()) {
                requestResponse.setFailure("ERROR: Statement already exists for loanId " + statementHeader.getLoanId() + " and statement date " + statementHeader.getStatementDate());
                return;
            }
            Optional<Statement> lastStatement = statementService.findLastStatement(statementHeader.getLoanId());
            // determine interest balance
            BigDecimal interestBalance = lastStatement.isPresent() ? lastStatement.get().getEndingBalance() : statementHeader.getLoanFunding();
            boolean paymentCreditFound = statementHeader.getRegisterEntries().stream().anyMatch(re -> re.getCredit() != null);
            // so, let's do interest and fee calculations here.
            if (!paymentCreditFound) {
                RegisterEntryMessage feeRegisterEntry = (RegisterEntryMessage) mqProducers.accountBillingCycleCharge(session, BillingCycleCharge.builder().id(statementHeader.getFeeChargeId()).loanId(statementHeader.getLoanId()).date(statementHeader.getStatementDate()).debit(new BigDecimal("30.00")).description("Non payment fee").retry(statementHeader.getRetry()).build());
                statementHeader.getRegisterEntries().add(feeRegisterEntry);
            }
            BigDecimal interestAmt = interestBalance.multiply(interestRate).divide(interestMonths, 2, RoundingMode.HALF_EVEN);
            RegisterEntryMessage interestRegisterEntry = (RegisterEntryMessage) mqProducers.accountBillingCycleCharge(session, BillingCycleCharge.builder().id(statementHeader.getInterestChargeId()).loanId(statementHeader.getLoanId()).date(statementHeader.getStatementDate()).debit(interestAmt).description("Interest").retry(statementHeader.getRetry()).build());
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
            requestResponse.setSuccess();
            if (endingBalance.compareTo(BigDecimal.ZERO) <= 0) {
                mqProducers.accountLoanClosed(session, statementHeader);
                loanClosed = true;
            }
        } catch (Exception ex) {
            log.error(LOG_STRING, ex);
            requestResponse.setError(ex.getMessage());
        } finally {
            if (!loanClosed) {
                try {
                    mqProducers.serviceRequestStatementComplete(session, requestResponse);
                } catch (JMSException innerEx) {
                    log.error("ERROR SENDING ERROR", innerEx);
                }
            }
        }
    }

    public void onQueryMostRecentStatementMessage(Message message) {
        try {
            UUID loanId = (UUID) ((ObjectMessage) message).getObject();
            log.debug("onQueryMostRecentStatementMessage {}", loanId);
            MostRecentStatement mostRecentStatement = queryService.findMostRecentStatement(loanId).map(statement -> MostRecentStatement.builder().id(statement.getId()).loanId(loanId).statementDate(statement.getStatementDate()).endingBalance(statement.getEndingBalance()).startingBalance(statement.getStartingBalance()).build()).orElse(MostRecentStatement.builder().loanId(loanId).build());

            reply(session, message, mostRecentStatement);
            log.debug("onQueryMostRecentStatementMessage completed {}", loanId);
        } catch (Exception ex) {
            log.error("onQueryMostRecentStatementMessage exception", ex);
        }
    }

    public void onQueryStatementMessage(Message message) {
        try {
            UUID loanId = (UUID) ((ObjectMessage) message).getObject();
            log.debug("onQueryStatementMessage {}", loanId);
            String result = queryService.findById(loanId).map(Statement::getStatementDoc).orElse("ERROR: No statement found for id " + loanId);
            reply(session, message, result);
        } catch (Exception ex) {
            log.error("onQueryStatementMessage exception", ex);
        }

    }

    public void onQueryStatementsMessage(Message message) {
        try {
            UUID id = (UUID) ((ObjectMessage) message).getObject();
            log.debug("onQueryStatementsMessage {}", id);
            reply(session, message, objectMapper.writeValueAsString(queryService.findByLoanId(id)));
        } catch (Exception ex) {
            log.error("onQueryStatementsMessage exception", ex);
        }
    }

    public void onCloseStatementMessage(Message message) {
        StatementHeader statementHeader = null;
        try {
            statementHeader = (StatementHeader) ((ObjectMessage) message).getObject();
        } catch (JMSException e) {
            log.error("onCloseStatementMessage", e);
        }
        try {
            log.debug("onCloseStatementMessage {}", statementHeader);
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
            mqProducers.accountLoanClosed(session, statementHeader);
        } catch (Exception ex) {
            log.error("onCloseStatementMessage", ex);
            try {
                ServiceRequestResponse requestResponse = new ServiceRequestResponse(statementHeader.getId(), ServiceRequestMessage.STATUS.ERROR, ex.getMessage());
                mqProducers.serviceRequestServiceRequest(session, requestResponse);
            } catch (Exception innerEx) {
                log.error("ERROR SENDING ERROR", innerEx);
            }
        }
    }

    public void reply(Session session, Message consumerMessage, Serializable data) throws JMSException {
        Message message = session.createObjectMessage(data);
        message.setJMSCorrelationID(consumerMessage.getJMSCorrelationID());
        try (MessageProducer producer = session.createProducer(consumerMessage.getJMSReplyTo())) {
            producer.send(message);
        }
    }
}