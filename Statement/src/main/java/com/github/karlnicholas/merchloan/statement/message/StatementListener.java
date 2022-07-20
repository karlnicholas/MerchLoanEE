package com.github.karlnicholas.merchloan.statement.message;

import com.github.karlnicholas.merchloan.accountsinterface.message.AccountsEjb;
import com.github.karlnicholas.merchloan.jmsmessage.BillingCycleCharge;
import com.github.karlnicholas.merchloan.jmsmessage.RegisterEntryMessage;
import com.github.karlnicholas.merchloan.jmsmessage.StatementCompleteResponse;
import com.github.karlnicholas.merchloan.jmsmessage.StatementHeader;
import com.github.karlnicholas.merchloan.statement.model.Statement;
import com.github.karlnicholas.merchloan.statement.service.StatementService;
import lombok.extern.slf4j.Slf4j;


import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@MessageDriven(name = "StatementMDB", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/StatementStatementQueue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
@Slf4j
public class StatementListener implements MessageListener {
    @Resource(lookup = "java:comp/DefaultJMSConnectionFactory")
    private ConnectionFactory connectionFactory;
    private JMSContext jmsContext;
    private JMSProducer jmsProducer;
    @Resource(lookup = "java:global/jms/queue/ServiceRequestStatementCompleteQueue")
    private Queue serviceRequestStatementCompleteQueue;
    @Resource(lookup = "java:global/jms/queue/AccountLoanClosedQueue")
    private Queue accountLoanClosedQueue;

    @PostConstruct
    public void postConstruct() {
        jmsContext = connectionFactory.createContext();
        jmsProducer = jmsContext.createProducer().setDeliveryMode(DeliveryMode.NON_PERSISTENT);
    }
    @PreDestroy
    public void preDestroy() {
        jmsContext.close();
    }
    @Inject
    private StatementService statementService;
    private final BigDecimal interestRate = new BigDecimal("0.10");
    private final BigDecimal interestMonths = new BigDecimal("12");
    @EJB(lookup = "ejb:merchloanee/accounts/AccountsEjbImpl!com.github.karlnicholas.merchloan.accountsinterface.message.AccountsEjb")
    private AccountsEjb accountsEjb;

    @Override
    public void onMessage(Message message) {
        StatementHeader statementHeader;
        try {
            statementHeader = (StatementHeader) ((ObjectMessage) message).getObject();
        } catch (JMSException e) {
            log.error("StatementListener", e);
            return;
        }
        StatementCompleteResponse requestResponse = StatementCompleteResponse.builder()
                .id(statementHeader.getId())
                .statementDate(statementHeader.getStatementDate())
                .loanId(statementHeader.getLoanId())
                .build();
        boolean loanClosed = false;
        try {
            log.debug("StatementListener {}", statementHeader);
            statementHeader = accountsEjb.statementHeader(statementHeader);
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
                RegisterEntryMessage feeRegisterEntry = accountsEjb.billingCycleCharge(BillingCycleCharge.builder().id(statementHeader.getFeeChargeId()).loanId(statementHeader.getLoanId()).date(statementHeader.getStatementDate()).debit(new BigDecimal("30.00")).description("Non payment fee").retry(statementHeader.getRetry()).build());
                statementHeader.getRegisterEntries().add(feeRegisterEntry);
            }
            BigDecimal interestAmt = interestBalance.multiply(interestRate).divide(interestMonths, 2, RoundingMode.HALF_EVEN);
            RegisterEntryMessage interestRegisterEntry = accountsEjb.billingCycleCharge(BillingCycleCharge.builder().id(statementHeader.getInterestChargeId()).loanId(statementHeader.getLoanId()).date(statementHeader.getStatementDate()).debit(interestAmt).description("Interest").retry(statementHeader.getRetry()).build());
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
                jmsProducer.send(accountLoanClosedQueue, statementHeader);
                loanClosed = true;
            }
        } catch (Exception ex) {
            log.error("StatementListener", ex);
            requestResponse.setError(ex.getMessage());
        } finally {
            if (!loanClosed) {
                jmsProducer.send(serviceRequestStatementCompleteQueue, requestResponse);
            }
        }
    }
}
