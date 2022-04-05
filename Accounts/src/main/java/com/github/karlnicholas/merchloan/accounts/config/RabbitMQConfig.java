package com.github.karlnicholas.merchloan.accounts.config;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Configuration
@Slf4j
public class RabbitMQConfig {
    private final RabbitMqProperties rabbitMqProperties;
    private final Channel replyChannel;
    private final AccountManagementService accountManagementService;
    private final RegisterManagementService registerManagementService;
    private final ObjectMapper objectMapper;
    private final QueryService queryService;

    public RabbitMQConfig(RabbitMqProperties rabbitMqProperties, ConnectionFactory connectionFactory, RabbitMqReceiver rabbitMqReceiver, AccountManagementService accountManagementService, RegisterManagementService registerManagementService, ObjectMapper objectMapper, QueryService queryService) throws IOException, TimeoutException {
        this.rabbitMqProperties = rabbitMqProperties;
        this.accountManagementService = accountManagementService;
        this.registerManagementService = registerManagementService;
        this.objectMapper = objectMapper;
        this.queryService = queryService;
        Connection connection = connectionFactory.newConnection();

//                .with(rabbitMqProperties.getAccountCreateaccountRoutingKey())
        Channel accountCreateAccountChannel = connection.createChannel();
        accountCreateAccountChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        accountCreateAccountChannel.queueDeclare(rabbitMqProperties.getAccountCreateaccountQueue(), false, false, false, null);
        accountCreateAccountChannel.exchangeBind(rabbitMqProperties.getAccountCreateaccountQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountCreateaccountQueue());
        accountCreateAccountChannel.basicConsume(rabbitMqProperties.getAccountCreateaccountQueue(), true, rabbitMqReceiver::receivedCreateAccountMessage, consumerTag -> {});

//                .with(rabbitMqProperties.getAccountFundingRoutingKey())
        Channel accountFundingChannel = connection.createChannel();
        accountFundingChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        accountFundingChannel.queueDeclare(rabbitMqProperties.getAccountFundingQueue(), false, false, false, null);
        accountFundingChannel.exchangeBind(rabbitMqProperties.getAccountFundingQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountFundingQueue());
        accountFundingChannel.basicConsume(rabbitMqProperties.getAccountFundingQueue(), true, rabbitMqReceiver::receivedFundingMessage, consumerTag -> {});

//                .with(rabbitMqProperties.getAccountValidateCreditRoutingkey())
        Channel accountValidateCreditChannel = connection.createChannel();
        accountValidateCreditChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        accountValidateCreditChannel.queueDeclare(rabbitMqProperties.getAccountValidateCreditQueue(), false, false, false, null);
        accountValidateCreditChannel.exchangeBind(rabbitMqProperties.getAccountValidateCreditQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountValidateCreditQueue());
        accountValidateCreditChannel.basicConsume(rabbitMqProperties.getAccountValidateCreditQueue(), true, rabbitMqReceiver::receivedValidateCreditMessage, consumerTag -> {});

//                .with(rabbitMqProperties.getAccountValidateDebitRoutingkey())
        Channel accountValidateDebitChannel = connection.createChannel();
        accountValidateDebitChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        accountValidateDebitChannel.queueDeclare(rabbitMqProperties.getAccountValidateDebitQueue(), false, false, false, null);
        accountValidateDebitChannel.exchangeBind(rabbitMqProperties.getAccountValidateDebitQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountValidateDebitQueue());
        accountValidateDebitChannel.basicConsume(rabbitMqProperties.getAccountValidateDebitQueue(), true, rabbitMqReceiver::receivedValidateDebitMessage, consumerTag -> {});

//                .with(rabbitMqProperties.getAccountCloseLoanRoutingkey())
        Channel accountCloseLoanChannel = connection.createChannel();
        accountCloseLoanChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        accountCloseLoanChannel.queueDeclare(rabbitMqProperties.getAccountCloseLoanQueue(), false, false, false, null);
        accountCloseLoanChannel.exchangeBind(rabbitMqProperties.getAccountCloseLoanQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountCloseLoanQueue());
        accountCloseLoanChannel.basicConsume(rabbitMqProperties.getAccountCloseLoanQueue(), true, rabbitMqReceiver::receivedCloseLoanMessage, consumerTag -> {});

//                .with(rabbitMqProperties.getAccountLoanClosedRoutingkey())
        Channel accountLoanClosedChannel = connection.createChannel();
        accountLoanClosedChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        accountLoanClosedChannel.queueDeclare(rabbitMqProperties.getAccountLoanClosedQueue(), false, false, false, null);
        accountLoanClosedChannel.exchangeBind(rabbitMqProperties.getAccountLoanClosedQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountLoanClosedQueue());
        accountLoanClosedChannel.basicConsume(rabbitMqProperties.getAccountLoanClosedQueue(), true, rabbitMqReceiver::receivedLoanClosedMessage, consumerTag -> {});

//                .with(rabbitMqProperties.getAccountQueryStatementHeaderRoutingKey())
        Channel accountQueryStatementHeaderChannel = connection.createChannel();
        accountQueryStatementHeaderChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        accountQueryStatementHeaderChannel.queueDeclare(rabbitMqProperties.getAccountQueryStatementHeaderQueue(), false, false, false, null);
        accountQueryStatementHeaderChannel.exchangeBind(rabbitMqProperties.getAccountQueryStatementHeaderQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountQueryStatementHeaderQueue());
        accountQueryStatementHeaderChannel.basicConsume(rabbitMqProperties.getAccountQueryStatementHeaderQueue(), true, this::receivedStatementHeaderMessage, consumerTag -> {});

//                .with(rabbitMqProperties.getAccountBillingCycleChargeRoutingKey())
        Channel accountBillingCycleChargeChannel = connection.createChannel();
        accountBillingCycleChargeChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        accountBillingCycleChargeChannel.queueDeclare(rabbitMqProperties.getAccountBillingCycleChargeQueue(), false, false, false, null);
        accountBillingCycleChargeChannel.exchangeBind(rabbitMqProperties.getAccountBillingCycleChargeQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountBillingCycleChargeQueue());
        accountBillingCycleChargeChannel.basicConsume(rabbitMqProperties.getAccountBillingCycleChargeQueue(), true, this::receivedBillingCycleChargeMessage, consumerTag -> {});

//                .with(rabbitMqProperties.getAccountQueryLoansToCycleRoutingkey())
        Channel accountQueryLoansToCycle = connection.createChannel();
        accountQueryLoansToCycle.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        accountQueryLoansToCycle.queueDeclare(rabbitMqProperties.getAccountQueryLoansToCycleQueue(), false, false, false, null);
        accountQueryLoansToCycle.exchangeBind(rabbitMqProperties.getAccountQueryLoansToCycleQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountQueryLoansToCycleQueue());
        accountQueryLoansToCycle.basicConsume(rabbitMqProperties.getAccountQueryLoansToCycleQueue(), true, this::receivedLoansToCyceMessage, consumerTag -> {});

//                .with(rabbitMqProperties.getAccountQueryAccountIdRoutingKey())
        Channel accountQueryAccountId = connection.createChannel();
        accountQueryAccountId.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        accountQueryAccountId.queueDeclare(rabbitMqProperties.getAccountQueryAccountIdQueue(), false, false, false, null);
        accountQueryAccountId.exchangeBind(rabbitMqProperties.getAccountQueryAccountIdQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountQueryAccountIdQueue());
        accountQueryAccountId.basicConsume(rabbitMqProperties.getAccountQueryAccountIdQueue(), true, this::receivedQueryAccountIdMessage, consumerTag -> {});

//                .with(rabbitMqProperties.getAccountQueryLoanIdRoutingKey())
        Channel accountQueryLoanId = connection.createChannel();
        accountQueryLoanId.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        accountQueryLoanId.queueDeclare(rabbitMqProperties.getAccountQueryLoanIdQueue(), false, false, false, null);
        accountQueryLoanId.exchangeBind(rabbitMqProperties.getAccountQueryLoanIdQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountQueryLoanIdQueue());
        accountQueryLoanId.basicConsume(rabbitMqProperties.getAccountQueryLoanIdQueue(), true, this::receivedQueryLoanIdMessage, consumerTag -> {});

        connection = connectionFactory.newConnection();
        replyChannel = connection.createChannel();
        replyChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        replyChannel.queueDeclare(rabbitMqProperties.getServicerequestQueryIdQueue(), false, false, false, null);
        replyChannel.exchangeBind(rabbitMqProperties.getServicerequestQueryIdQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getServicerequestQueryIdQueue());
    }

    public void receivedStatementHeaderMessage(String consumerTag, Delivery delivery) {
        StatementHeader statementHeader = (StatementHeader) SerializationUtils.deserialize(delivery.getBody());
        try {
            log.debug("receivedStatementHeaderMessage {}", statementHeader);
            ServiceRequestResponse serviceRequestResponse = accountManagementService.statementHeader(statementHeader);
            if (serviceRequestResponse.isSuccess())
                registerManagementService.setStatementHeaderRegisterEntryies(statementHeader);
            reply(delivery, objectMapper.writeValueAsString(statementHeader).getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            log.error("receivedStatementHeaderMessage exception {}", ex.getMessage());
        }
    }

    public void receivedLoansToCyceMessage(String consumerTag, Delivery delivery) {
        LocalDate businessDate = (LocalDate) SerializationUtils.deserialize(delivery.getBody());
        try {
            log.debug("receivedLoansToCyceMessage {}", businessDate);
            reply(delivery, objectMapper.writeValueAsString(accountManagementService.loansToCycle(businessDate)).getBytes(StandardCharsets.UTF_8));
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
            reply(delivery, objectMapper.writeValueAsString(registerEntryMessage).getBytes(StandardCharsets.UTF_8));
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
                reply(delivery, objectMapper.writeValueAsString(r.get()).getBytes(StandardCharsets.UTF_8));
            } else {
                reply(delivery, ("ERROR: id not found: " + id).getBytes(StandardCharsets.UTF_8));
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
                reply(delivery, objectMapper.writeValueAsString(r.get()).getBytes(StandardCharsets.UTF_8));
            } else {
                reply(delivery, ("ERROR: Loan not found for id: " + id).getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception ex) {
            log.error("receivedQueryLoanIdMessage exception {}", ex.getMessage());
        }
    }

    private void reply(Delivery delivery, byte[] data) throws IOException {
        AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                .Builder()
                .correlationId(delivery.getProperties().getCorrelationId())
                .build();
        replyChannel.basicPublish(rabbitMqProperties.getExchange(), delivery.getProperties().getReplyTo(), replyProps, data);
        replyChannel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
    }
}
