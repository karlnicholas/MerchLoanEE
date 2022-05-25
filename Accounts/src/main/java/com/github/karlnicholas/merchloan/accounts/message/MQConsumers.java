package com.github.karlnicholas.merchloan.accounts.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.karlnicholas.merchloan.accounts.model.Account;
import com.github.karlnicholas.merchloan.accounts.model.RegisterEntry;
import com.github.karlnicholas.merchloan.accounts.service.AccountManagementService;
import com.github.karlnicholas.merchloan.accounts.service.QueryService;
import com.github.karlnicholas.merchloan.accounts.service.RegisterManagementService;
import com.github.karlnicholas.merchloan.dto.LoanDto;
import com.github.karlnicholas.merchloan.jms.MQConsumerUtils;
import com.github.karlnicholas.merchloan.jmsmessage.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.jms.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class MQConsumers {
    private final AccountManagementService accountManagementService;
    private final RegisterManagementService registerManagementService;
    private final QueryService queryService;
    private final ObjectMapper objectMapper;
    private final MQProducers mqProducers;
    private final JMSContext accountCreateAccountContext;
    private final JMSContext accountFundingContext;
    private final JMSContext accountValidateCreditContext;
    private final JMSContext accountValidateDebitContext;
    private final JMSContext accountCloseLoanContext;
    private final JMSContext accountLoanClosedContext;
    private final JMSContext accountQueryStatementHeaderContext;
    private final JMSContext accountBillingCycleChargeContext;
    private final JMSContext accountQueryLoansToCycleContext;
    private final JMSContext accountQueryAccountIdContext;
    private final JMSContext accountQueryLoanIdContext;

    public MQConsumers(ConnectionFactory connectionFactory, MQProducers mqProducers, MQConsumerUtils mqConsumerUtils, AccountManagementService accountManagementService, RegisterManagementService registerManagementService, QueryService queryService) throws JMSException {
        this.mqProducers = mqProducers;
        this.accountManagementService = accountManagementService;
        this.registerManagementService = registerManagementService;
        this.queryService = queryService;
        this.objectMapper = new ObjectMapper().findAndRegisterModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        accountCreateAccountContext = connectionFactory.createContext();
        accountCreateAccountContext.setClientID("Account::accountCreateAccountContext");
        accountCreateAccountContext.createConsumer(accountCreateAccountContext.createQueue(mqConsumerUtils.getAccountCreateAccountQueue())).setMessageListener(this::receivedCreateAccountMessage);

        accountFundingContext = connectionFactory.createContext();
        accountFundingContext.setClientID("Account::accountFundingContext");
        accountFundingContext.createConsumer(accountFundingContext.createQueue(mqConsumerUtils.getAccountFundingQueue())).setMessageListener(this::receivedFundingMessage);

        accountValidateCreditContext = connectionFactory.createContext();
        accountValidateCreditContext.setClientID("Account::accountValidateCreditContext");
        accountValidateCreditContext.createConsumer(accountValidateCreditContext.createQueue(mqConsumerUtils.getAccountValidateCreditQueue())).setMessageListener(this::receivedValidateCreditMessage);

        accountValidateDebitContext = connectionFactory.createContext();
        accountValidateDebitContext.setClientID("Account::accountValidateDebitContext");
        accountValidateDebitContext.createConsumer(accountValidateDebitContext.createQueue(mqConsumerUtils.getAccountValidateDebitQueue())).setMessageListener(this::receivedValidateDebitMessage);

        accountCloseLoanContext = connectionFactory.createContext();
        accountCloseLoanContext.setClientID("Account::accountCloseLoanContext");
        accountCloseLoanContext.createConsumer(accountCloseLoanContext.createQueue(mqConsumerUtils.getAccountCloseLoanQueue())).setMessageListener(this::receivedCloseLoanMessage);

        accountLoanClosedContext = connectionFactory.createContext();
        accountLoanClosedContext.setClientID("Account::accountLoanClosedContext");
        accountLoanClosedContext.createConsumer(accountLoanClosedContext.createQueue(mqConsumerUtils.getAccountLoanClosedQueue())).setMessageListener(this::receivedLoanClosedMessage);

        accountQueryStatementHeaderContext = connectionFactory.createContext();
        accountQueryStatementHeaderContext.setClientID("Account::accountQueryStatementHeaderContext");
        accountQueryStatementHeaderContext.createConsumer(accountQueryStatementHeaderContext.createQueue(mqConsumerUtils.getAccountQueryStatementHeaderQueue())).setMessageListener(this::receivedStatementHeaderMessage);

        accountBillingCycleChargeContext = connectionFactory.createContext();
        accountBillingCycleChargeContext.setClientID("Account::accountBillingCycleChargeContext");
        accountBillingCycleChargeContext.createConsumer(accountBillingCycleChargeContext.createQueue(mqConsumerUtils.getAccountBillingCycleChargeQueue())).setMessageListener(this::receivedBillingCycleChargeMessage);

        accountQueryLoansToCycleContext = connectionFactory.createContext();
        accountQueryLoansToCycleContext.setClientID("Account::accountQueryLoansToCycleContext");
        accountQueryLoansToCycleContext.createConsumer(accountQueryLoansToCycleContext.createQueue(mqConsumerUtils.getAccountQueryLoansToCycleQueue())).setMessageListener(this::receivedLoansToCycleMessage);

        accountQueryAccountIdContext = connectionFactory.createContext();
        accountQueryAccountIdContext.setClientID("Account::accountQueryAccountIdContext");
        accountQueryAccountIdContext.createConsumer(accountQueryAccountIdContext.createQueue(mqConsumerUtils.getAccountQueryAccountIdQueue())).setMessageListener(this::receivedQueryAccountIdMessage);

        accountQueryLoanIdContext = connectionFactory.createContext();
        accountQueryLoanIdContext.setClientID("Account::accountQueryLoanIdContext");
        accountQueryLoanIdContext.createConsumer(accountQueryLoanIdContext.createQueue(mqConsumerUtils.getAccountQueryLoanIdQueue())).setMessageListener(this::receivedQueryLoanIdMessage);
    }

    public void receivedStatementHeaderMessage(Message message) {
        try {
            StatementHeader statementHeader = (StatementHeader) ((ObjectMessage) message).getObject();
            log.debug("receivedStatementHeaderMessage {}", statementHeader);
            ServiceRequestResponse serviceRequestResponse = accountManagementService.statementHeader(statementHeader);
            if (serviceRequestResponse.isSuccess())
                registerManagementService.setStatementHeaderRegisterEntryies(statementHeader);
            reply(accountQueryStatementHeaderContext, message, statementHeader);
        } catch (Exception ex) {
            log.error("receivedStatementHeaderMessage exception {}", ex.getMessage());
        }
    }

    public void receivedLoansToCycleMessage(Message message) {
        try {
            LocalDate businessDate = (LocalDate) ((ObjectMessage) message).getObject();
            log.trace("receivedLoansToCycleMessage: {}", businessDate);
            reply(accountQueryLoansToCycleContext, message, (Serializable) accountManagementService.loansToCycle(businessDate));
        } catch (Exception ex) {
            log.error("receivedLoansToCycleMessage exception {}", ex.getMessage());
        }
    }

    public void receivedBillingCycleChargeMessage(Message message) {
        try {
            BillingCycleCharge billingCycleCharge = (BillingCycleCharge) ((ObjectMessage) message).getObject();
            log.debug("receivedBillingCycleChargeMessage: {}", billingCycleCharge);
            RegisterEntry re = registerManagementService.billingCycleCharge(billingCycleCharge);
            RegisterEntryMessage registerEntryMessage = RegisterEntryMessage.builder()
                    .date(re.getDate())
                    .credit(re.getCredit())
                    .debit(re.getDebit())
                    .description(re.getDescription())
                    .timeStamp(re.getTimeStamp())
                    .build();
            reply(accountBillingCycleChargeContext, message, registerEntryMessage);
        } catch (Exception ex) {
            log.error("receivedBillingCycleChargeMessage exception {}", ex.getMessage());
        }
    }

    public void receivedQueryAccountIdMessage(Message message) {
        try {
            UUID id = (UUID) ((ObjectMessage) message).getObject();
            log.debug("receivedQueryAccountIdMessage: {}", id);
            Optional<Account> accountOpt = queryService.queryAccountId(id);
            if (accountOpt.isPresent()) {
                reply(accountQueryAccountIdContext, message, objectMapper.writeValueAsString(accountOpt.get()));
            } else {
                reply(accountQueryAccountIdContext, message, "ERROR: id not found: " + id);
            }
        } catch (Exception ex) {
            log.error("receivedQueryAccountIdMessage exception {}", ex.getMessage());
        }
    }

    public void receivedQueryLoanIdMessage(Message message) {
        try {
            UUID id = (UUID) ((ObjectMessage) message).getObject();
            log.debug("receivedQueryLoanIdMessage: {}", id);
            Optional<LoanDto> r = queryService.queryLoanId(id);
            if (r.isPresent()) {
                reply(accountQueryLoanIdContext, message, objectMapper.writeValueAsString(r.get()));
            } else {
                reply(accountQueryLoanIdContext, message, ("ERROR: Loan not found for id: " + id));
            }
            log.debug("receivedQueryLoanIdMessage complete: {}", id);
        } catch (Exception ex) {
            log.error("receivedQueryLoanIdMessage exception {}", ex.getMessage());
        }
    }

    public void reply(JMSContext context, Message consumerMessage, Serializable data) throws JMSException {
        Message message = context.createObjectMessage(data);
        message.setJMSCorrelationID(consumerMessage.getJMSCorrelationID());
        context.createProducer().send(consumerMessage.getJMSReplyTo(), message);
    }

    public void receivedCreateAccountMessage(Message message) {
        CreateAccount createAccount = null;
        try {
            createAccount = (CreateAccount) ((ObjectMessage) message).getObject();
        } catch (JMSException e) {
            log.error("receivedCreateAccountMessage exception {}", e);
        }
        ServiceRequestResponse requestResponse = ServiceRequestResponse.builder().id(createAccount.getId()).build();
        try {
            log.debug("receivedCreateAccountMessage: {}", createAccount);
            accountManagementService.createAccount(createAccount, requestResponse);
        } catch (Exception ex) {
            log.error("receivedCreateAccountMessage exception {}", ex.getMessage());
            requestResponse.setError(ex.getMessage());
        } finally {
            try {
                mqProducers.serviceRequestServiceRequest(requestResponse);
            } catch (JMSException e) {
                log.error("receivedCreateAccountMessage exception {}", e);
            }
        }
    }

    public void receivedFundingMessage(Message message) {
        // M= P [r (1+r)^n/ ((1+r)^n)-1)]
        // r = .10 / 12 = 0.00833
        // 10000 * 0.00833(1.00833)^12 / ((1.00833)^12)-1]
        // 10000 * 0.0092059/0.104713067
        // 10000 * 0.08791548
        // = 879.16
        FundLoan fundLoan = null;
        try {
            fundLoan = (FundLoan) ((ObjectMessage) message).getObject();
        } catch (JMSException e) {
            log.error("receivedFundingMessage exception", e);
        }
        ServiceRequestResponse requestResponse = ServiceRequestResponse.builder()
                .id(fundLoan.getId())
                .build();
        try {
            log.debug("receivedFundingMessage: {} ", fundLoan);
            accountManagementService.fundAccount(fundLoan, requestResponse);
            if (requestResponse.isSuccess()) {
                registerManagementService.fundLoan(
                        DebitLoan.builder()
                                .id(fundLoan.getId())
                                .amount(fundLoan.getAmount())
                                .date(fundLoan.getStartDate())
                                .loanId(fundLoan.getId())
                                .description(fundLoan.getDescription())
                                .build(),
                        requestResponse);
            }
        } catch (Exception ex) {
            log.error("receivedFundingMessage exception", ex);
            requestResponse.setError(ex.getMessage());
        } finally {
            try {
                mqProducers.serviceRequestServiceRequest(requestResponse);
            } catch (JMSException e) {
                log.error("receivedFundingMessage exception", e.getMessage());
            }
        }
    }

    public void receivedValidateCreditMessage(Message message) {
        CreditLoan creditLoan = null;
        try {
            creditLoan = (CreditLoan) ((ObjectMessage) message).getObject();
        } catch (JMSException e) {
            log.error("receivedValidateCreditMessage exception", e);
        }
        ServiceRequestResponse requestResponse = ServiceRequestResponse.builder()
                .id(creditLoan.getId())
                .build();
        try {
            log.debug("receivedValidateCreditMessage: {} ", creditLoan);
            accountManagementService.validateLoan(creditLoan.getLoanId(), requestResponse);
            if (requestResponse.isSuccess()) {
                registerManagementService.creditLoan(CreditLoan.builder()
                        .id(creditLoan.getId())
                        .amount(creditLoan.getAmount())
                        .date(creditLoan.getDate())
                        .loanId(creditLoan.getLoanId())
                        .description(creditLoan.getDescription())
                        .build(), requestResponse);
            }
        } catch (Exception ex) {
            log.error("receivedValidateCreditMessage exception", ex);
            requestResponse.setError(ex.getMessage());
        } finally {
            try {
                mqProducers.serviceRequestServiceRequest(requestResponse);
            } catch (JMSException e) {
                log.error("receivedValidateCreditMessage exception", e);
            }
        }
    }

    public void receivedValidateDebitMessage(Message message) {
        DebitLoan debitLoan = null;
        try {
            debitLoan = (DebitLoan) ((ObjectMessage) message).getObject();
        } catch (JMSException e) {
            log.error("receivedValidateDebitMessage exception", e);
        }
        ServiceRequestResponse requestResponse = ServiceRequestResponse.builder()
                .id(debitLoan.getId())
                .build();
        try {
            log.debug("receivedValidateDebitMessage: {} ", debitLoan);
            accountManagementService.validateLoan(debitLoan.getLoanId(), requestResponse);
            if (requestResponse.isSuccess()) {
                registerManagementService.debitLoan(DebitLoan.builder()
                                .id(debitLoan.getId())
                                .amount(debitLoan.getAmount())
                                .date(debitLoan.getDate())
                                .loanId(debitLoan.getLoanId())
                                .description(debitLoan.getDescription())
                                .build(),
                        requestResponse);
            }
        } catch (Exception ex) {
            log.error("receivedValidateDebitMessage exception", ex);
            requestResponse.setError(ex.getMessage());
        } finally {
            try {
                mqProducers.serviceRequestServiceRequest(requestResponse);
            } catch (JMSException e) {
                log.error("receivedValidateDebitMessage exception", e);
            }
        }
    }

    public void receivedCloseLoanMessage(Message message) {
        CloseLoan closeLoan = null;
        try {
            closeLoan = (CloseLoan) ((ObjectMessage) message).getObject();
        } catch (JMSException e) {
            log.error("receivedCloseLoanMessage exception", e);
        }
        ServiceRequestResponse serviceRequestResponse = ServiceRequestResponse.builder().id(closeLoan.getId()).build();
        try {
            log.debug("receivedCloseLoanMessage: {} ", closeLoan);
            Optional<LoanDto> loanOpt = queryService.queryLoanId(closeLoan.getLoanId());
            if (loanOpt.isPresent()) {
                if (closeLoan.getAmount().compareTo(loanOpt.get().getPayoffAmount()) == 0) {
                    registerManagementService.debitLoan(DebitLoan.builder()
                                    .id(closeLoan.getInterestChargeId())
                                    .loanId(closeLoan.getLoanId())
                                    .date(closeLoan.getDate())
                                    .amount(loanOpt.get().getCurrentInterest())
                                    .description("Interest")
                                    .build()
                            , serviceRequestResponse);
                    // determine interest balance
                    registerManagementService.creditLoan(CreditLoan.builder()
                                    .id(closeLoan.getPaymentId())
                                    .loanId(closeLoan.getLoanId())
                                    .date(closeLoan.getDate())
                                    .amount(closeLoan.getAmount())
                                    .description("Payoff Payment")
                                    .build()
                            , serviceRequestResponse);
                    closeLoan.setLoanDto(loanOpt.get());
                    closeLoan.setLastStatementDate(loanOpt.get().getLastStatementDate());
                    StatementHeader statementHeader = StatementHeader.builder()
                            .id(closeLoan.getId())
                            .accountId(closeLoan.getLoanDto().getAccountId())
                            .loanId(closeLoan.getLoanId())
                            .statementDate(closeLoan.getDate())
                            .startDate(closeLoan.getLastStatementDate().plusDays(1))
                            .endDate(closeLoan.getDate())
                            .build();
                    registerManagementService.setStatementHeaderRegisterEntryies(statementHeader);
                    mqProducers.statementCloseStatement(statementHeader);
                } else {
                    serviceRequestResponse.setFailure("PayoffAmount incorrect. Required: " + loanOpt.get().getPayoffAmount());
                }
            } else {
                serviceRequestResponse.setFailure("loan not found for id: " + closeLoan.getLoanId());
            }
        } catch (Exception ex) {
            log.error("receivedCloseLoanMessage exception", ex);
            serviceRequestResponse.setError("receivedCloseLoanMessage exception " + ex.getMessage());
        } finally {
            try {
                mqProducers.serviceRequestServiceRequest(serviceRequestResponse);
            } catch (JMSException e) {
                log.error("receivedCloseLoanMessage exception", e);
            }
        }
    }

    public void receivedLoanClosedMessage(Message message) {
        StatementHeader statementHeader = null;
        try {
            statementHeader = (StatementHeader) ((ObjectMessage) message).getObject();
        } catch (JMSException e) {
            log.error("receivedLoanClosedMessage exception", e);
        }
        ServiceRequestResponse serviceRequestResponse = ServiceRequestResponse.builder().id(statementHeader.getId()).build();
        try {
            log.debug("receivedLoanClosedMessage: {} ", statementHeader);
            accountManagementService.closeLoan(statementHeader.getLoanId());
            serviceRequestResponse.setSuccess();
        } catch (Exception ex) {
            log.error("receivedLoanClosedMessage exception", ex);
            serviceRequestResponse.setError("receivedLoanClosedMessage exception: " + ex.getMessage());
        } finally {
            try {
                mqProducers.serviceRequestServiceRequest(serviceRequestResponse);
            } catch (JMSException e) {
                log.error("receivedLoanClosedMessage exception", e);
            }
        }
    }

}
