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
    private final Connection connection;
    private final Session session;
    private final AccountManagementService accountManagementService;
    private final RegisterManagementService registerManagementService;
    private final QueryService queryService;
    private final ObjectMapper objectMapper;
    private final MQProducers rabbitMqSender;

    public MQConsumers(Connection connection, MQProducers rabbitMqSender, MQConsumerUtils mqConsumerUtils, AccountManagementService accountManagementService, RegisterManagementService registerManagementService, QueryService queryService) throws JMSException {
        this.connection = connection;
        session = connection.createSession();
        this.accountManagementService = accountManagementService;
        this.registerManagementService = registerManagementService;
        this.queryService = queryService;
        this.rabbitMqSender = rabbitMqSender;
        this.objectMapper = new ObjectMapper().findAndRegisterModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        mqConsumerUtils.bindConsumer(session, session.createQueue(mqConsumerUtils.getAccountCreateAccountQueue()), this::receivedCreateAccountMessage);
        mqConsumerUtils.bindConsumer(session, session.createQueue(mqConsumerUtils.getAccountFundingQueue()), this::receivedFundingMessage);
        mqConsumerUtils.bindConsumer(session, session.createQueue(mqConsumerUtils.getAccountValidateCreditQueue()), this::receivedValidateCreditMessage);
        mqConsumerUtils.bindConsumer(session, session.createQueue(mqConsumerUtils.getAccountValidateDebitQueue()), this::receivedValidateDebitMessage);
        mqConsumerUtils.bindConsumer(session, session.createQueue(mqConsumerUtils.getAccountCloseLoanQueue()), this::receivedCloseLoanMessage);
        mqConsumerUtils.bindConsumer(session, session.createQueue(mqConsumerUtils.getAccountLoanClosedQueue()), this::receivedLoanClosedMessage);
        mqConsumerUtils.bindConsumer(session, session.createQueue(mqConsumerUtils.getAccountQueryStatementHeaderQueue()), this::receivedStatementHeaderMessage);
        mqConsumerUtils.bindConsumer(session, session.createQueue(mqConsumerUtils.getAccountBillingCycleChargeQueue()), this::receivedBillingCycleChargeMessage);
        mqConsumerUtils.bindConsumer(session, session.createQueue(mqConsumerUtils.getAccountQueryLoansToCycleQueue()), this::receivedLoansToCycleMessage);
        mqConsumerUtils.bindConsumer(session, session.createQueue(mqConsumerUtils.getAccountQueryAccountIdQueue()), this::receivedQueryAccountIdMessage);
        mqConsumerUtils.bindConsumer(session, session.createQueue(mqConsumerUtils.getAccountQueryLoanIdQueue()), this::receivedQueryLoanIdMessage);
        connection.start();
    }

    public void receivedStatementHeaderMessage(Message message) {
        try {
            StatementHeader statementHeader = (StatementHeader) ((ObjectMessage) message).getObject();
            log.debug("receivedStatementHeaderMessage {}", statementHeader);
            ServiceRequestResponse serviceRequestResponse = accountManagementService.statementHeader(statementHeader);
            if (serviceRequestResponse.isSuccess())
                registerManagementService.setStatementHeaderRegisterEntryies(statementHeader);
            reply(session, message, statementHeader);
        } catch (Exception ex) {
            log.error("receivedStatementHeaderMessage exception {}", ex.getMessage());
        }
    }

    public void receivedLoansToCycleMessage(Message message) {
        try {
            LocalDate businessDate = (LocalDate) ((ObjectMessage) message).getObject();
            log.trace("receivedLoansToCycleMessage: {}", businessDate);
            reply(session, message, (Serializable) accountManagementService.loansToCycle(businessDate));
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
            reply(session, message, registerEntryMessage);
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
                reply(session, message, objectMapper.writeValueAsString(accountOpt.get()));
            } else {
                reply(session, message, "ERROR: id not found: " + id);
            }
        } catch (Exception ex) {
            log.error("receivedQueryAccountIdMessage exception {}", ex.getMessage());
        }
    }

    public void receivedQueryLoanIdMessage(Message message) {
        try {
            UUID id = (UUID) ((ObjectMessage) message).getObject();
            log.debug("receivedQueryLoanIdMessage: {}", id);
            Optional<LoanDto> r = queryService.queryLoanId(session, id);
            if (r.isPresent()) {
                reply(session, message, objectMapper.writeValueAsString(r.get()));
            } else {
                reply(session, message, ("ERROR: Loan not found for id: " + id));
            }
            log.debug("receivedQueryLoanIdMessage complete: {}", id);
        } catch (Exception ex) {
            log.error("receivedQueryLoanIdMessage exception {}", ex.getMessage());
        }
    }

    public void reply(Session session, Message consumerMessage, Serializable data) throws JMSException {
        Message message = session.createObjectMessage(data);
        message.setJMSCorrelationID(consumerMessage.getJMSCorrelationID());
        try (MessageProducer producer = session.createProducer(consumerMessage.getJMSReplyTo())) {
            producer.send(message);
        }
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
                rabbitMqSender.serviceRequestServiceRequest(session, requestResponse);
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
                rabbitMqSender.serviceRequestServiceRequest(session, requestResponse);
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
                rabbitMqSender.serviceRequestServiceRequest(session, requestResponse);
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
                rabbitMqSender.serviceRequestServiceRequest(session, requestResponse);
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
            Optional<LoanDto> loanOpt = queryService.queryLoanId(session, closeLoan.getLoanId());
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
                    rabbitMqSender.statementCloseStatement(session, statementHeader);
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
                rabbitMqSender.serviceRequestServiceRequest(session, serviceRequestResponse);
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
                rabbitMqSender.serviceRequestServiceRequest(session, serviceRequestResponse);
            } catch (JMSException e) {
                log.error("receivedLoanClosedMessage exception", e);
            }
        }
    }

}
