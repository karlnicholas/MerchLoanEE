package com.github.karlnicholas.merchloan.accounts.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.karlnicholas.merchloan.accounts.model.Account;
import com.github.karlnicholas.merchloan.accounts.model.RegisterEntry;
import com.github.karlnicholas.merchloan.accounts.service.AccountManagementService;
import com.github.karlnicholas.merchloan.accounts.service.QueryService;
import com.github.karlnicholas.merchloan.accounts.service.RegisterManagementService;
import com.github.karlnicholas.merchloan.dto.LoanDto;
import com.github.karlnicholas.merchloan.jms.config.MQQueueNames;
import com.github.karlnicholas.merchloan.jmsmessage.*;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.io.IOException;
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
    private final MQProducers rabbitMqSender;
    private final MQQueueNames mqQueueNames;
    private final Channel responseChannel;


    public MQConsumers(Connection connection, MQProducers rabbitMqSender, MQQueueNames mqQueueNames, AccountManagementService accountManagementService, RegisterManagementService registerManagementService, QueryService queryService) throws IOException {
        this.accountManagementService = accountManagementService;
        this.registerManagementService = registerManagementService;
        this.queryService = queryService;
        this.rabbitMqSender = rabbitMqSender;
        this.objectMapper = new ObjectMapper().findAndRegisterModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        this.mqQueueNames = mqQueueNames;

//                .with(rabbitMqProperties.getAccountCreateaccountRoutingKey())
        Channel accountCreateaccountChannel = connection.createChannel();
        accountCreateaccountChannel.exchangeDeclare(mqQueueNames.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        accountCreateaccountChannel.queueDeclare(mqQueueNames.getAccountCreateaccountQueue(), false, true, true, null);
        accountCreateaccountChannel.queueBind(mqQueueNames.getAccountCreateaccountQueue(), mqQueueNames.getExchange(), mqQueueNames.getAccountCreateaccountQueue());
        accountCreateaccountChannel.basicConsume(mqQueueNames.getAccountCreateaccountQueue(), true, this::receivedCreateAccountMessage, consumerTag -> {});

//                .with(rabbitMqProperties.getAccountFundingRoutingKey())
        Channel accountFundingChannel = connection.createChannel();
        accountFundingChannel.exchangeDeclare(mqQueueNames.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        accountFundingChannel.queueDeclare(mqQueueNames.getAccountFundingQueue(), false, true, true, null);
        accountFundingChannel.queueBind(mqQueueNames.getAccountFundingQueue(), mqQueueNames.getExchange(), mqQueueNames.getAccountFundingQueue());
        accountFundingChannel.basicConsume(mqQueueNames.getAccountFundingQueue(), true, this::receivedFundingMessage, consumerTag -> {});

//                .with(rabbitMqProperties.getAccountValidateCreditRoutingkey())
        Channel accountValidateCreditChannel = connection.createChannel();
        accountValidateCreditChannel.exchangeDeclare(mqQueueNames.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        accountValidateCreditChannel.queueDeclare(mqQueueNames.getAccountValidateCreditQueue(), false, true, true, null);
        accountValidateCreditChannel.queueBind(mqQueueNames.getAccountValidateCreditQueue(), mqQueueNames.getExchange(), mqQueueNames.getAccountValidateCreditQueue());
        accountValidateCreditChannel.basicConsume(mqQueueNames.getAccountValidateCreditQueue(), true, this::receivedValidateCreditMessage, consumerTag -> {});

//                .with(rabbitMqProperties.getAccountValidateDebitRoutingkey())
        Channel accountValidateDebitChannel = connection.createChannel();
        accountValidateDebitChannel.exchangeDeclare(mqQueueNames.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        accountValidateDebitChannel.queueDeclare(mqQueueNames.getAccountValidateDebitQueue(), false, true, true, null);
        accountValidateDebitChannel.queueBind(mqQueueNames.getAccountValidateDebitQueue(), mqQueueNames.getExchange(), mqQueueNames.getAccountValidateDebitQueue());
        accountValidateDebitChannel.basicConsume(mqQueueNames.getAccountValidateDebitQueue(), true, this::receivedValidateDebitMessage, consumerTag -> {});

//                .with(rabbitMqProperties.getAccountCloseLoanRoutingkey())
        Channel accountCloseLoanChannel = connection.createChannel();
        accountCloseLoanChannel.exchangeDeclare(mqQueueNames.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        accountCloseLoanChannel.queueDeclare(mqQueueNames.getAccountCloseLoanQueue(), false, true, true, null);
        accountCloseLoanChannel.queueBind(mqQueueNames.getAccountCloseLoanQueue(), mqQueueNames.getExchange(), mqQueueNames.getAccountCloseLoanQueue());
        accountCloseLoanChannel.basicConsume(mqQueueNames.getAccountCloseLoanQueue(), true, this::receivedCloseLoanMessage, consumerTag -> {});

//                .with(rabbitMqProperties.getAccountLoanClosedRoutingkey())
        Channel accountLoanClosedChannel = connection.createChannel();
        accountLoanClosedChannel.exchangeDeclare(mqQueueNames.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        accountLoanClosedChannel.queueDeclare(mqQueueNames.getAccountLoanClosedQueue(), false, true, true, null);
        accountLoanClosedChannel.queueBind(mqQueueNames.getAccountLoanClosedQueue(), mqQueueNames.getExchange(), mqQueueNames.getAccountLoanClosedQueue());
        accountLoanClosedChannel.basicConsume(mqQueueNames.getAccountLoanClosedQueue(), true, this::receivedLoanClosedMessage, consumerTag -> {});

//                .with(rabbitMqProperties.getAccountQueryStatementHeaderRoutingKey())
        Channel accountQueryStatementHeaderChannel = connection.createChannel();
        accountQueryStatementHeaderChannel.exchangeDeclare(mqQueueNames.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        accountQueryStatementHeaderChannel.queueDeclare(mqQueueNames.getAccountQueryStatementHeaderQueue(), false, true, true, null);
        accountQueryStatementHeaderChannel.queueBind(mqQueueNames.getAccountQueryStatementHeaderQueue(), mqQueueNames.getExchange(), mqQueueNames.getAccountQueryStatementHeaderQueue());
        accountQueryStatementHeaderChannel.basicConsume(mqQueueNames.getAccountQueryStatementHeaderQueue(), true, this::receivedStatementHeaderMessage, consumerTag -> {});

//                .with(rabbitMqProperties.getAccountBillingCycleChargeRoutingKey())
        Channel accountBillingCycleChargeChannel = connection.createChannel();
        accountBillingCycleChargeChannel.exchangeDeclare(mqQueueNames.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        accountBillingCycleChargeChannel.queueDeclare(mqQueueNames.getAccountBillingCycleChargeQueue(), false, true, true, null);
        accountBillingCycleChargeChannel.queueBind(mqQueueNames.getAccountBillingCycleChargeQueue(), mqQueueNames.getExchange(), mqQueueNames.getAccountBillingCycleChargeQueue());
        accountBillingCycleChargeChannel.basicConsume(mqQueueNames.getAccountBillingCycleChargeQueue(), true, this::receivedBillingCycleChargeMessage, consumerTag -> {});

//                .with(rabbitMqProperties.getAccountQueryLoansToCycleRoutingkey())
        Channel accountQueryLoansToCycleChannel = connection.createChannel();
        accountQueryLoansToCycleChannel.exchangeDeclare(mqQueueNames.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        accountQueryLoansToCycleChannel.queueDeclare(mqQueueNames.getAccountQueryLoansToCycleQueue(), false, true, true, null);
        accountQueryLoansToCycleChannel.queueBind(mqQueueNames.getAccountQueryLoansToCycleQueue(), mqQueueNames.getExchange(), mqQueueNames.getAccountQueryLoansToCycleQueue());
        accountQueryLoansToCycleChannel.basicConsume(mqQueueNames.getAccountQueryLoansToCycleQueue(), true, this::receivedLoansToCyceMessage, consumerTag -> {});

//                .with(rabbitMqProperties.getAccountQueryAccountIdRoutingKey())
        Channel accountQueryAccountIdChannel = connection.createChannel();
        accountQueryAccountIdChannel.exchangeDeclare(mqQueueNames.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        accountQueryAccountIdChannel.queueDeclare(mqQueueNames.getAccountQueryAccountIdQueue(), false, true, true, null);
        accountQueryAccountIdChannel.queueBind(mqQueueNames.getAccountQueryAccountIdQueue(), mqQueueNames.getExchange(), mqQueueNames.getAccountQueryAccountIdQueue());
        accountQueryAccountIdChannel.basicConsume(mqQueueNames.getAccountQueryAccountIdQueue(), true, this::receivedQueryAccountIdMessage, consumerTag -> {});

//                .with(rabbitMqProperties.getAccountQueryLoanIdRoutingKey())
        Channel accountQueryLoanIdQueue = connection.createChannel();
        accountQueryLoanIdQueue.exchangeDeclare(mqQueueNames.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        accountQueryLoanIdQueue.queueDeclare(mqQueueNames.getAccountQueryLoanIdQueue(), false, true, true, null);
        accountQueryLoanIdQueue.queueBind(mqQueueNames.getAccountQueryLoanIdQueue(), mqQueueNames.getExchange(), mqQueueNames.getAccountQueryLoanIdQueue());
        accountQueryLoanIdQueue.basicConsume(mqQueueNames.getAccountQueryLoanIdQueue(), true, this::receivedQueryLoanIdMessage, consumerTag -> {});

        responseChannel = connection.createChannel();
    }

    public void receivedStatementHeaderMessage(String consumerTag, Delivery delivery) {
        StatementHeader statementHeader = (StatementHeader) SerializationUtils.deserialize(delivery.getBody());
        try {
            log.debug("receivedStatementHeaderMessage {}", statementHeader);
            ServiceRequestResponse serviceRequestResponse = accountManagementService.statementHeader(statementHeader);
            if (serviceRequestResponse.isSuccess())
                registerManagementService.setStatementHeaderRegisterEntryies(statementHeader);
            reply(delivery, statementHeader);
        } catch (Exception ex) {
            log.error("receivedStatementHeaderMessage exception {}", ex.getMessage());
        }
    }

    public void receivedLoansToCyceMessage(String consumerTag, Delivery delivery) {
        LocalDate businessDate = (LocalDate) SerializationUtils.deserialize(delivery.getBody());
        try {
            log.debug("receivedLoansToCyceMessage {}", businessDate);
            reply(delivery, accountManagementService.loansToCycle(businessDate));
        } catch (Exception ex) {
            log.error("receivedLoansToCyceMessage exception {}", ex.getMessage());
        }
    }

    public void receivedBillingCycleChargeMessage(String consumerTag, Delivery delivery) {
        BillingCycleCharge billingCycleCharge = (BillingCycleCharge) SerializationUtils.deserialize(delivery.getBody());
        try {
            log.debug("receivedBillingCycleChargeMessage {}", billingCycleCharge);
            RegisterEntry re = registerManagementService.billingCycleCharge(billingCycleCharge);
            RegisterEntryMessage registerEntryMessage = RegisterEntryMessage.builder()
                    .date(re.getDate())
                    .credit(re.getCredit())
                    .debit(re.getDebit())
                    .description(re.getDescription())
                    .timeStamp(re.getTimeStamp())
                    .build();
            reply(delivery, registerEntryMessage);
        } catch (Exception ex) {
            log.error("receivedBillingCycleChargeMessage exception {}", ex.getMessage());
        }
    }

    public void receivedQueryAccountIdMessage(String consumerTag, Delivery delivery) {
        UUID id = (UUID) SerializationUtils.deserialize(delivery.getBody());
        try {
            log.debug("receivedQueryAccountIdMessage {}", id);
            Optional<Account> accountOpt = queryService.queryAccountId(id);
            if (accountOpt.isPresent()) {
                reply(delivery, objectMapper.writeValueAsString(accountOpt.get()));
            } else {
                reply(delivery, "ERROR: id not found: " + id);
            }
        } catch (Exception ex) {
            log.error("receivedQueryAccountIdMessage exception {}", ex.getMessage());
        }
    }

    public void receivedQueryLoanIdMessage(String consumerTag, Delivery delivery) {
        UUID id = (UUID) SerializationUtils.deserialize(delivery.getBody());
        try {
            log.debug("receivedQueryLoanIdMessage {}", id);
            Optional<LoanDto> r = queryService.queryLoanId(id);
            if (r.isPresent()) {
                reply(delivery, objectMapper.writeValueAsString(r.get()));
            } else {
                reply(delivery, ("ERROR: Loan not found for id: " + id));
            }
        } catch (Exception ex) {
            log.error("receivedQueryLoanIdMessage exception {}", ex.getMessage());
        }
    }

    private void reply(Delivery delivery, Object data) throws IOException {
        AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                .Builder()
                .correlationId(delivery.getProperties().getCorrelationId())
                .build();
        responseChannel.basicPublish(mqQueueNames.getExchange(), delivery.getProperties().getReplyTo(), replyProps, SerializationUtils.serialize(data));
    }

    public void receivedCreateAccountMessage(String consumerTag, Delivery delivery) throws IOException {
        CreateAccount createAccount = (CreateAccount) SerializationUtils.deserialize(delivery.getBody());
        ServiceRequestResponse requestResponse = ServiceRequestResponse.builder().id(createAccount.getId()).build();
        try {
            log.debug("receivedCreateAccountMessage{}", createAccount);
            accountManagementService.createAccount(createAccount, requestResponse);
        } catch (Exception ex) {
            log.error("receivedCreateAccountMessage exception {}", ex.getMessage());
            requestResponse.setError(ex.getMessage());
        } finally {
            rabbitMqSender.serviceRequestServiceRequest(requestResponse);
        }
    }

    public void receivedFundingMessage(String consumerTag, Delivery delivery) throws IOException {
        // M= P [r (1+r)^n/ ((1+r)^n)-1)]
        // r = .10 / 12 = 0.00833
        // 10000 * 0.00833(1.00833)^12 / ((1.00833)^12)-1]
        // 10000 * 0.0092059/0.104713067
        // 10000 * 0.08791548
        // = 879.16
        FundLoan fundLoan = (FundLoan) SerializationUtils.deserialize(delivery.getBody());
        ServiceRequestResponse requestResponse = ServiceRequestResponse.builder()
                .id(fundLoan.getId())
                .build();
        try {
            log.debug("receivedFundingMessage {} ", fundLoan);
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
            log.error("receivedFundingMessage exception {}", ex.getMessage());
            requestResponse.setError(ex.getMessage());
        } finally {
            rabbitMqSender.serviceRequestServiceRequest(requestResponse);
        }
    }

    public void receivedValidateCreditMessage(String consumerTag, Delivery delivery) throws IOException {
        CreditLoan creditLoan = (CreditLoan) SerializationUtils.deserialize(delivery.getBody());
        ServiceRequestResponse requestResponse = ServiceRequestResponse.builder()
                .id(creditLoan.getId())
                .build();
        try {
            log.debug("receivedValidateCreditMessage {} ", creditLoan);
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
            log.error("receivedValidateCreditMessage exception {}", ex.getMessage());
            requestResponse.setError(ex.getMessage());
        } finally {
            rabbitMqSender.serviceRequestServiceRequest(requestResponse);
        }
    }

    public void receivedValidateDebitMessage(String consumerTag, Delivery delivery) throws IOException {
        DebitLoan debitLoan = (DebitLoan) SerializationUtils.deserialize(delivery.getBody());
        ServiceRequestResponse requestResponse = ServiceRequestResponse.builder()
                .id(debitLoan.getId())
                .build();
        try {
            log.debug("receivedValidateDebitMessage {} ", debitLoan);
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
            log.error("receivedValidateDebitMessage exception {}", ex.getMessage());
            requestResponse.setError(ex.getMessage());
        } finally {
            rabbitMqSender.serviceRequestServiceRequest(requestResponse);
        }
    }

    public void receivedCloseLoanMessage(String consumerTag, Delivery delivery) throws IOException {
        CloseLoan closeLoan = (CloseLoan) SerializationUtils.deserialize(delivery.getBody());
        ServiceRequestResponse serviceRequestResponse = ServiceRequestResponse.builder().id(closeLoan.getId()).build();
        try {
            log.debug("receivedCloseLoanMessage {} ", closeLoan);
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
                    rabbitMqSender.statementCloseStatement(statementHeader);
                } else {
                    serviceRequestResponse.setFailure("PayoffAmount incorrect. Required: " + loanOpt.get().getPayoffAmount());
                }
            } else {
                serviceRequestResponse.setFailure("loan not found for id: " + closeLoan.getLoanId());
            }
        } catch (Exception ex) {
            log.error("receivedCloseLoanMessage exception {}", ex.getMessage());
            serviceRequestResponse.setFailure("receivedCloseLoanMessage exception " + ex.getMessage());
        } finally {
            rabbitMqSender.serviceRequestServiceRequest(serviceRequestResponse);
        }
    }

    public void receivedLoanClosedMessage(String consumerTag, Delivery delivery) throws IOException {
        StatementHeader statementHeader = (StatementHeader) SerializationUtils.deserialize(delivery.getBody());
        ServiceRequestResponse serviceRequestResponse = ServiceRequestResponse.builder().id(statementHeader.getId()).build();
        try {
            log.debug("receivedLoanClosedMessage {} ", statementHeader);
            accountManagementService.closeLoan(statementHeader.getLoanId());
            serviceRequestResponse.setSuccess();
        } catch (Exception ex) {
            log.error("receivedLoanClosedMessage exception {}", ex.getMessage());
            serviceRequestResponse.setFailure(ex.getMessage());
        } finally {
            rabbitMqSender.serviceRequestServiceRequest(serviceRequestResponse);
        }
    }

}
