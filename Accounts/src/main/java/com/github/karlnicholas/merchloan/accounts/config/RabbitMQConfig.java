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
import java.nio.charset.StandardCharsets;
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

    public RabbitMQConfig(RabbitMqProperties rabbitMqProperties, ConnectionFactory connectionFactory, RabbitMqReceiver rabbitMqReceiver, AccountManagementService accountManagementService, RegisterManagementService registerManagementService, QueryService queryService) throws IOException, TimeoutException {
        this.rabbitMqProperties = rabbitMqProperties;
        this.accountManagementService = accountManagementService;
        this.registerManagementService = registerManagementService;
        this.objectMapper = new ObjectMapper().findAndRegisterModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        this.queryService = queryService;
        Connection connection = connectionFactory.newConnection();
        Channel accountReceiveChannel = connection.createChannel();

//                .with(rabbitMqProperties.getAccountCreateaccountRoutingKey())
        accountReceiveChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        accountReceiveChannel.queueDeclare(rabbitMqProperties.getAccountCreateaccountQueue(), false, true, true, null);
        accountReceiveChannel.queueBind(rabbitMqProperties.getAccountCreateaccountQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountCreateaccountQueue());
        accountReceiveChannel.basicConsume(rabbitMqProperties.getAccountCreateaccountQueue(), true, rabbitMqReceiver::receivedCreateAccountMessage, consumerTag -> {});

//                .with(rabbitMqProperties.getAccountFundingRoutingKey())
        accountReceiveChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        accountReceiveChannel.queueDeclare(rabbitMqProperties.getAccountFundingQueue(), false, true, true, null);
        accountReceiveChannel.queueBind(rabbitMqProperties.getAccountFundingQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountFundingQueue());
        accountReceiveChannel.basicConsume(rabbitMqProperties.getAccountFundingQueue(), true, rabbitMqReceiver::receivedFundingMessage, consumerTag -> {});

//                .with(rabbitMqProperties.getAccountValidateCreditRoutingkey())
        accountReceiveChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        accountReceiveChannel.queueDeclare(rabbitMqProperties.getAccountValidateCreditQueue(), false, true, true, null);
        accountReceiveChannel.queueBind(rabbitMqProperties.getAccountValidateCreditQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountValidateCreditQueue());
        accountReceiveChannel.basicConsume(rabbitMqProperties.getAccountValidateCreditQueue(), true, rabbitMqReceiver::receivedValidateCreditMessage, consumerTag -> {});

//                .with(rabbitMqProperties.getAccountValidateDebitRoutingkey())
        accountReceiveChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        accountReceiveChannel.queueDeclare(rabbitMqProperties.getAccountValidateDebitQueue(), false, true, true, null);
        accountReceiveChannel.queueBind(rabbitMqProperties.getAccountValidateDebitQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountValidateDebitQueue());
        accountReceiveChannel.basicConsume(rabbitMqProperties.getAccountValidateDebitQueue(), true, rabbitMqReceiver::receivedValidateDebitMessage, consumerTag -> {});

//                .with(rabbitMqProperties.getAccountCloseLoanRoutingkey())
        accountReceiveChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        accountReceiveChannel.queueDeclare(rabbitMqProperties.getAccountCloseLoanQueue(), false, true, true, null);
        accountReceiveChannel.queueBind(rabbitMqProperties.getAccountCloseLoanQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountCloseLoanQueue());
        accountReceiveChannel.basicConsume(rabbitMqProperties.getAccountCloseLoanQueue(), true, rabbitMqReceiver::receivedCloseLoanMessage, consumerTag -> {});

//                .with(rabbitMqProperties.getAccountLoanClosedRoutingkey())
        accountReceiveChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        accountReceiveChannel.queueDeclare(rabbitMqProperties.getAccountLoanClosedQueue(), false, true, true, null);
        accountReceiveChannel.queueBind(rabbitMqProperties.getAccountLoanClosedQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountLoanClosedQueue());
        accountReceiveChannel.basicConsume(rabbitMqProperties.getAccountLoanClosedQueue(), true, rabbitMqReceiver::receivedLoanClosedMessage, consumerTag -> {});

//                .with(rabbitMqProperties.getAccountQueryStatementHeaderRoutingKey())
        accountReceiveChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        accountReceiveChannel.queueDeclare(rabbitMqProperties.getAccountQueryStatementHeaderQueue(), false, true, true, null);
        accountReceiveChannel.queueBind(rabbitMqProperties.getAccountQueryStatementHeaderQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountQueryStatementHeaderQueue());
        accountReceiveChannel.basicConsume(rabbitMqProperties.getAccountQueryStatementHeaderQueue(), true, this::receivedStatementHeaderMessage, consumerTag -> {});

//                .with(rabbitMqProperties.getAccountBillingCycleChargeRoutingKey())
        accountReceiveChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        accountReceiveChannel.queueDeclare(rabbitMqProperties.getAccountBillingCycleChargeQueue(), false, true, true, null);
        accountReceiveChannel.queueBind(rabbitMqProperties.getAccountBillingCycleChargeQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountBillingCycleChargeQueue());
        accountReceiveChannel.basicConsume(rabbitMqProperties.getAccountBillingCycleChargeQueue(), true, this::receivedBillingCycleChargeMessage, consumerTag -> {});

//                .with(rabbitMqProperties.getAccountQueryLoansToCycleRoutingkey())
        accountReceiveChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        accountReceiveChannel.queueDeclare(rabbitMqProperties.getAccountQueryLoansToCycleQueue(), false, true, true, null);
        accountReceiveChannel.queueBind(rabbitMqProperties.getAccountQueryLoansToCycleQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountQueryLoansToCycleQueue());
        accountReceiveChannel.basicConsume(rabbitMqProperties.getAccountQueryLoansToCycleQueue(), true, this::receivedLoansToCyceMessage, consumerTag -> {});

//                .with(rabbitMqProperties.getAccountQueryAccountIdRoutingKey())
        accountReceiveChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        accountReceiveChannel.queueDeclare(rabbitMqProperties.getAccountQueryAccountIdQueue(), false, true, true, null);
        accountReceiveChannel.queueBind(rabbitMqProperties.getAccountQueryAccountIdQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountQueryAccountIdQueue());
        accountReceiveChannel.basicConsume(rabbitMqProperties.getAccountQueryAccountIdQueue(), true, this::receivedQueryAccountIdMessage, consumerTag -> {});

//                .with(rabbitMqProperties.getAccountQueryLoanIdRoutingKey())
        accountReceiveChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        accountReceiveChannel.queueDeclare(rabbitMqProperties.getAccountQueryLoanIdQueue(), false, true, true, null);
        accountReceiveChannel.queueBind(rabbitMqProperties.getAccountQueryLoanIdQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountQueryLoanIdQueue());
        accountReceiveChannel.basicConsume(rabbitMqProperties.getAccountQueryLoanIdQueue(), true, this::receivedQueryLoanIdMessage, consumerTag -> {});

        connection = connectionFactory.newConnection();
        responseChannel = connection.createChannel();
//        responseChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT,false, true, null);
//        responseChannel.queueDeclare(rabbitMqProperties.getServicerequestQueryIdQueue(), false, true, true, null);
//        responseChannel.queueBind(rabbitMqProperties.getServicerequestQueryIdQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getServicerequestQueryIdQueue());
    }

    public void receivedStatementHeaderMessage(String consumerTag, Delivery delivery) {
        StatementHeader statementHeader = (StatementHeader) SerializationUtils.deserialize(delivery.getBody());
        try {
            log.debug("receivedStatementHeaderMessage {}", statementHeader);
            ServiceRequestResponse serviceRequestResponse = accountManagementService.statementHeader(statementHeader);
            if (serviceRequestResponse.isSuccess())
                registerManagementService.setStatementHeaderRegisterEntryies(statementHeader);
            reply(delivery, objectMapper.writeValueAsString(statementHeader));
        } catch (Exception ex) {
            log.error("receivedStatementHeaderMessage exception {}", ex.getMessage());
        }
    }

    public void receivedLoansToCyceMessage(String consumerTag, Delivery delivery) {
        LocalDate businessDate = (LocalDate) SerializationUtils.deserialize(delivery.getBody());
        try {
            log.debug("receivedLoansToCyceMessage {}", businessDate);
            reply(delivery, objectMapper.writeValueAsString(accountManagementService.loansToCycle(businessDate)));
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
            reply(delivery, objectMapper.writeValueAsString(registerEntryMessage));
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
