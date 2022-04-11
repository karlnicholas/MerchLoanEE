package com.github.karlnicholas.merchloan.accounts.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.karlnicholas.merchloan.accounts.message.RabbitMqReceiver;
import com.github.karlnicholas.merchloan.accounts.model.Account;
import com.github.karlnicholas.merchloan.accounts.model.RegisterEntry;
import com.github.karlnicholas.merchloan.accounts.service.AccountManagementService;
import com.github.karlnicholas.merchloan.accounts.service.QueryService;
import com.github.karlnicholas.merchloan.accounts.service.RegisterManagementService;
import com.github.karlnicholas.merchloan.dto.LoanDto;
import com.github.karlnicholas.merchloan.jms.config.RabbitMqProperties;
import com.github.karlnicholas.merchloan.jmsmessage.*;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.SerializationUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Configuration
@Slf4j
public class RabbitMQConfig {
    private final RabbitMqProperties rabbitMqProperties;
    private final Channel responseChannel;
    private final AccountManagementService accountManagementService;
    private final RegisterManagementService registerManagementService;
    private final ObjectMapper objectMapper;
    private final QueryService queryService;

    public RabbitMQConfig(RabbitMqProperties rabbitMqProperties, Connection connection, RabbitMqReceiver rabbitMqReceiver, AccountManagementService accountManagementService, RegisterManagementService registerManagementService, QueryService queryService) throws IOException, TimeoutException {
        this.rabbitMqProperties = rabbitMqProperties;
        this.accountManagementService = accountManagementService;
        this.registerManagementService = registerManagementService;
        this.objectMapper = new ObjectMapper().findAndRegisterModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        this.queryService = queryService;

//                .with(rabbitMqProperties.getAccountCreateaccountRoutingKey())
        Channel accountCreateaccountChannel = connection.createChannel();
        accountCreateaccountChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        accountCreateaccountChannel.queueDeclare(rabbitMqProperties.getAccountCreateaccountQueue(), false, true, true, null);
        accountCreateaccountChannel.queueBind(rabbitMqProperties.getAccountCreateaccountQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountCreateaccountQueue());
        accountCreateaccountChannel.basicConsume(rabbitMqProperties.getAccountCreateaccountQueue(), true, rabbitMqReceiver::receivedCreateAccountMessage, consumerTag -> {});

//                .with(rabbitMqProperties.getAccountFundingRoutingKey())
        Channel accountFundingChannel = connection.createChannel();
        accountFundingChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        accountFundingChannel.queueDeclare(rabbitMqProperties.getAccountFundingQueue(), false, true, true, null);
        accountFundingChannel.queueBind(rabbitMqProperties.getAccountFundingQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountFundingQueue());
        accountFundingChannel.basicConsume(rabbitMqProperties.getAccountFundingQueue(), true, rabbitMqReceiver::receivedFundingMessage, consumerTag -> {});

//                .with(rabbitMqProperties.getAccountValidateCreditRoutingkey())
        Channel accountValidateCreditChannel = connection.createChannel();
        accountValidateCreditChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        accountValidateCreditChannel.queueDeclare(rabbitMqProperties.getAccountValidateCreditQueue(), false, true, true, null);
        accountValidateCreditChannel.queueBind(rabbitMqProperties.getAccountValidateCreditQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountValidateCreditQueue());
        accountValidateCreditChannel.basicConsume(rabbitMqProperties.getAccountValidateCreditQueue(), true, rabbitMqReceiver::receivedValidateCreditMessage, consumerTag -> {});

//                .with(rabbitMqProperties.getAccountValidateDebitRoutingkey())
        Channel accountValidateDebitChannel = connection.createChannel();
        accountValidateDebitChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        accountValidateDebitChannel.queueDeclare(rabbitMqProperties.getAccountValidateDebitQueue(), false, true, true, null);
        accountValidateDebitChannel.queueBind(rabbitMqProperties.getAccountValidateDebitQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountValidateDebitQueue());
        accountValidateDebitChannel.basicConsume(rabbitMqProperties.getAccountValidateDebitQueue(), true, rabbitMqReceiver::receivedValidateDebitMessage, consumerTag -> {});

//                .with(rabbitMqProperties.getAccountCloseLoanRoutingkey())
        Channel accountCloseLoanChannel = connection.createChannel();
        accountCloseLoanChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        accountCloseLoanChannel.queueDeclare(rabbitMqProperties.getAccountCloseLoanQueue(), false, true, true, null);
        accountCloseLoanChannel.queueBind(rabbitMqProperties.getAccountCloseLoanQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountCloseLoanQueue());
        accountCloseLoanChannel.basicConsume(rabbitMqProperties.getAccountCloseLoanQueue(), true, rabbitMqReceiver::receivedCloseLoanMessage, consumerTag -> {});

//                .with(rabbitMqProperties.getAccountLoanClosedRoutingkey())
        Channel accountLoanClosedChannel = connection.createChannel();
        accountLoanClosedChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        accountLoanClosedChannel.queueDeclare(rabbitMqProperties.getAccountLoanClosedQueue(), false, true, true, null);
        accountLoanClosedChannel.queueBind(rabbitMqProperties.getAccountLoanClosedQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountLoanClosedQueue());
        accountLoanClosedChannel.basicConsume(rabbitMqProperties.getAccountLoanClosedQueue(), true, rabbitMqReceiver::receivedLoanClosedMessage, consumerTag -> {});

//                .with(rabbitMqProperties.getAccountQueryStatementHeaderRoutingKey())
        Channel accountQueryStatementHeaderChannel = connection.createChannel();
        accountQueryStatementHeaderChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        accountQueryStatementHeaderChannel.queueDeclare(rabbitMqProperties.getAccountQueryStatementHeaderQueue(), false, true, true, null);
        accountQueryStatementHeaderChannel.queueBind(rabbitMqProperties.getAccountQueryStatementHeaderQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountQueryStatementHeaderQueue());
        accountQueryStatementHeaderChannel.basicConsume(rabbitMqProperties.getAccountQueryStatementHeaderQueue(), true, this::receivedStatementHeaderMessage, consumerTag -> {});

//                .with(rabbitMqProperties.getAccountBillingCycleChargeRoutingKey())
        Channel accountBillingCycleChargeChannel = connection.createChannel();
        accountBillingCycleChargeChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        accountBillingCycleChargeChannel.queueDeclare(rabbitMqProperties.getAccountBillingCycleChargeQueue(), false, true, true, null);
        accountBillingCycleChargeChannel.queueBind(rabbitMqProperties.getAccountBillingCycleChargeQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountBillingCycleChargeQueue());
        accountBillingCycleChargeChannel.basicConsume(rabbitMqProperties.getAccountBillingCycleChargeQueue(), true, this::receivedBillingCycleChargeMessage, consumerTag -> {});

//                .with(rabbitMqProperties.getAccountQueryLoansToCycleRoutingkey())
        Channel accountQueryLoansToCycleChannel = connection.createChannel();
        accountQueryLoansToCycleChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        accountQueryLoansToCycleChannel.queueDeclare(rabbitMqProperties.getAccountQueryLoansToCycleQueue(), false, true, true, null);
        accountQueryLoansToCycleChannel.queueBind(rabbitMqProperties.getAccountQueryLoansToCycleQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountQueryLoansToCycleQueue());
        accountQueryLoansToCycleChannel.basicConsume(rabbitMqProperties.getAccountQueryLoansToCycleQueue(), true, this::receivedLoansToCyceMessage, consumerTag -> {});

//                .with(rabbitMqProperties.getAccountQueryAccountIdRoutingKey())
        Channel accountQueryAccountIdChannel = connection.createChannel();
        accountQueryAccountIdChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        accountQueryAccountIdChannel.queueDeclare(rabbitMqProperties.getAccountQueryAccountIdQueue(), false, true, true, null);
        accountQueryAccountIdChannel.queueBind(rabbitMqProperties.getAccountQueryAccountIdQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountQueryAccountIdQueue());
        accountQueryAccountIdChannel.basicConsume(rabbitMqProperties.getAccountQueryAccountIdQueue(), true, this::receivedQueryAccountIdMessage, consumerTag -> {});

//                .with(rabbitMqProperties.getAccountQueryLoanIdRoutingKey())
        Channel accountQueryLoanIdQueue = connection.createChannel();
        accountQueryLoanIdQueue.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        accountQueryLoanIdQueue.queueDeclare(rabbitMqProperties.getAccountQueryLoanIdQueue(), false, true, true, null);
        accountQueryLoanIdQueue.queueBind(rabbitMqProperties.getAccountQueryLoanIdQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountQueryLoanIdQueue());
        accountQueryLoanIdQueue.basicConsume(rabbitMqProperties.getAccountQueryLoanIdQueue(), true, this::receivedQueryLoanIdMessage, consumerTag -> {});

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
                    .rowNum(re.getRowNum())
                    .date(re.getDate())
                    .credit(re.getCredit())
                    .debit(re.getDebit())
                    .description(re.getDescription())
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
            Optional<Account> r = queryService.queryAccountId(id);
            if (r.isPresent()) {
                reply(delivery, objectMapper.writeValueAsString(r.get()));
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
        responseChannel.basicPublish(rabbitMqProperties.getExchange(), delivery.getProperties().getReplyTo(), replyProps, SerializationUtils.serialize(data));
    }
}
