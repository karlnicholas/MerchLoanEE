package com.github.karlnicholas.merchloan.statement.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.karlnicholas.merchloan.jmsmessage.BillingCycleCharge;
import com.github.karlnicholas.merchloan.jmsmessage.RegisterEntryMessage;
import com.github.karlnicholas.merchloan.jmsmessage.StatementCompleteResponse;
import com.github.karlnicholas.merchloan.jmsmessage.StatementHeader;
import com.github.karlnicholas.merchloan.replywaiting.ReplyWaitingHandler;
import com.github.karlnicholas.merchloan.statement.model.Statement;
import com.github.karlnicholas.merchloan.statement.service.StatementService;
import lombok.extern.slf4j.Slf4j;


import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

@MessageDriven(name = "StatementMDB", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/StatementStatementQueue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
@Slf4j
public class StatementListener implements MessageListener {
    @Inject
    private JMSContext jmsContext;
    @Resource(lookup = "java:global/jms/queue/ServiceRequestStatementCompleteQueue")
    private Queue serviceRequestStatementCompleteQueue;
    @Resource(lookup = "java:global/jms/queue/AccountsLoanClosedQueue")
    private Queue accountLoanClosedQueue;
    @Inject
    private StatementService statementService;
    private final BigDecimal interestRate = new BigDecimal("0.10");
    private final BigDecimal interestMonths = new BigDecimal("12");
    @Resource(lookup = "java:global/jms/queue/AccountsStatementHeaderQueue")
    private Queue accountsStatementHeaderQueue;
    private TemporaryQueue accountsStatementHeaderReplyQueue;
    private ReplyWaitingHandler replyWaitingHandlerStatementHeader;
    @Resource(lookup = "java:global/jms/queue/AccountsBillingCycleChargeQueue")
    private Queue accountsBillingCycleChargeQueue;
    private TemporaryQueue accountsBillingCycleChargeReplyQueue;
    private ReplyWaitingHandler replyWaitingHandlerBillingCycleCharge;

    @PostConstruct
    public void postConstruct() {

        replyWaitingHandlerStatementHeader = new ReplyWaitingHandler();
        accountsStatementHeaderReplyQueue = jmsContext.createTemporaryQueue();
        JMSConsumer queryIdReplyConsumer = jmsContext.createConsumer(accountsStatementHeaderReplyQueue);
        queryIdReplyConsumer.setMessageListener(m-> {
            try {
                replyWaitingHandlerStatementHeader.handleReply(m.getJMSCorrelationID(), m.getBody(Object.class));
            } catch (JMSException e) {
                log.error("queryIdReplyConsumer ", e);
            }
        });

        replyWaitingHandlerBillingCycleCharge = new ReplyWaitingHandler();
        accountsBillingCycleChargeReplyQueue = jmsContext.createTemporaryQueue();
        JMSConsumer billingCycleReplyConsumer = jmsContext.createConsumer(accountsBillingCycleChargeReplyQueue);
        billingCycleReplyConsumer.setMessageListener(m-> {
            try {
                replyWaitingHandlerBillingCycleCharge.handleReply(m.getJMSCorrelationID(), m.getBody(Object.class));
            } catch (JMSException e) {
                log.error("billingCycleReplyConsumer ", e);
            }
        });
    }

    @Override
    public void onMessage(Message message) {
        boolean loanClosed = false;
        StatementHeader statementHeader = null;
        try {
            statementHeader = (StatementHeader) ((ObjectMessage) message).getObject();
        } catch (JMSException e) {
            throw new EJBException(e);
        }
        StatementCompleteResponse requestResponse = StatementCompleteResponse.builder()
                .id(statementHeader.getId())
                .statementDate(statementHeader.getStatementDate())
                .loanId(statementHeader.getLoanId())
                .build();
        try {
            log.debug("StatementListener {}", statementHeader);

//            statementHeader = accountsEjb.statementHeader(statementHeader);
            JMSProducer producer = jmsContext.createProducer();
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            ObjectMessage statementHeaderMessage = jmsContext.createObjectMessage(statementHeader);
            String correlationId = UUID.randomUUID().toString();
            statementHeaderMessage.setJMSReplyTo(accountsStatementHeaderReplyQueue);
            statementHeaderMessage.setJMSCorrelationID(correlationId);
            replyWaitingHandlerStatementHeader.put(correlationId);
            producer.send(accountsStatementHeaderQueue, statementHeaderMessage);
            statementHeader = (StatementHeader) replyWaitingHandlerStatementHeader.getReply(correlationId);

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
//                RegisterEntryMessage feeRegisterEntry = accountsEjb.billingCycleCharge(BillingCycleCharge.builder().id(statementHeader.getFeeChargeId()).loanId(statementHeader.getLoanId()).date(statementHeader.getStatementDate()).debit(new BigDecimal("30.00")).description("Non payment fee").retry(statementHeader.getRetry()).build());
                ObjectMessage billingChargeMessage = jmsContext.createObjectMessage(BillingCycleCharge.builder().id(statementHeader.getFeeChargeId()).loanId(statementHeader.getLoanId()).date(statementHeader.getStatementDate()).debit(new BigDecimal("30.00")).description("Non payment fee").retry(statementHeader.getRetry()).build());
                correlationId = UUID.randomUUID().toString();
                billingChargeMessage.setJMSReplyTo(accountsBillingCycleChargeReplyQueue);
                billingChargeMessage.setJMSCorrelationID(correlationId);
                replyWaitingHandlerBillingCycleCharge.put(correlationId);
                producer.send(accountsBillingCycleChargeQueue, statementHeaderMessage);
                RegisterEntryMessage feeRegisterEntry = (RegisterEntryMessage) replyWaitingHandlerBillingCycleCharge.getReply(correlationId);
                statementHeader.getRegisterEntries().add(feeRegisterEntry);
            }
            BigDecimal interestAmt = interestBalance.multiply(interestRate).divide(interestMonths, 2, RoundingMode.HALF_EVEN);
//            RegisterEntryMessage interestRegisterEntry = accountsEjb.billingCycleCharge(BillingCycleCharge.builder().id(statementHeader.getInterestChargeId()).loanId(statementHeader.getLoanId()).date(statementHeader.getStatementDate()).debit(interestAmt).description("Interest").retry(statementHeader.getRetry()).build());
            ObjectMessage billingChargeMessage = jmsContext.createObjectMessage(BillingCycleCharge.builder().id(statementHeader.getInterestChargeId()).loanId(statementHeader.getLoanId()).date(statementHeader.getStatementDate()).debit(interestAmt).description("Interest").retry(statementHeader.getRetry()).build());
            correlationId = UUID.randomUUID().toString();
            billingChargeMessage.setJMSReplyTo(accountsBillingCycleChargeReplyQueue);
            billingChargeMessage.setJMSCorrelationID(correlationId);
            replyWaitingHandlerBillingCycleCharge.put(correlationId);
            producer.send(accountsBillingCycleChargeQueue, statementHeaderMessage);
            RegisterEntryMessage interestRegisterEntry = (RegisterEntryMessage) replyWaitingHandlerBillingCycleCharge.getReply(correlationId);
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
                jmsContext.createProducer().setDeliveryMode(DeliveryMode.NON_PERSISTENT).send(accountLoanClosedQueue, statementHeader);
                loanClosed = true;
            }
        } catch (SQLException e) {
            log.error("StatementListener {}", e);
        } catch (JsonProcessingException e) {
            log.error("StatementListener {}", e);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (!loanClosed) {
                jmsContext.createProducer().setDeliveryMode(DeliveryMode.NON_PERSISTENT).send(serviceRequestStatementCompleteQueue, requestResponse);
            }
        }
    }
}
