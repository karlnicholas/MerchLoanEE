package com.github.karlnicholas.merchloan.statement.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.karlnicholas.merchloan.jms.config.RabbitMqProperties;
import com.github.karlnicholas.merchloan.jmsmessage.MostRecentStatement;
import com.github.karlnicholas.merchloan.statement.message.RabbitMqReceiver;
import com.github.karlnicholas.merchloan.statement.model.Statement;
import com.github.karlnicholas.merchloan.statement.service.QueryService;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.SerializationUtils;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Configuration
@Slf4j
public class RabbitMQConfig {
    private final RabbitMqProperties rabbitMqProperties;
    private final QueryService queryService;
    private Channel responseChannel;
    private final ObjectMapper objectMapper;

    public RabbitMQConfig(RabbitMqProperties rabbitMqProperties, RabbitMqReceiver rabbitMqReceiver, QueryService queryService, Connection connection) throws IOException, TimeoutException {
        this.rabbitMqProperties = rabbitMqProperties;
        this.queryService = queryService;
        this.objectMapper = new ObjectMapper().findAndRegisterModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);


//                .with(rabbitMqProperties.getStatementStatementRoutingkey())
        Channel statementStatementChannel = connection.createChannel();
        statementStatementChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        statementStatementChannel.queueDeclare(rabbitMqProperties.getStatementStatementQueue(), false, true, true, null);
        statementStatementChannel.queueBind(rabbitMqProperties.getStatementStatementQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getStatementStatementQueue());
        statementStatementChannel.basicConsume(rabbitMqProperties.getStatementStatementQueue(), true, rabbitMqReceiver::receivedStatementMessage, consumerTag -> {});

//                .with(rabbitMqProperties.getStatementCloseStatementRoutingkey())
        Channel statementCloseStatementChannel = connection.createChannel();
        statementCloseStatementChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        statementCloseStatementChannel.queueDeclare(rabbitMqProperties.getStatementCloseStatementQueue(), false, true, true, null);
        statementCloseStatementChannel.queueBind(rabbitMqProperties.getStatementCloseStatementQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getStatementCloseStatementQueue());
        statementCloseStatementChannel.basicConsume(rabbitMqProperties.getStatementCloseStatementQueue(), true, rabbitMqReceiver::receivedCloseStatementMessage, consumerTag -> {});
//                .with(rabbitMqProperties.getStatementQueryStatementRoutingkey())
        Channel statementQueryStatementChannel = connection.createChannel();
        statementQueryStatementChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        statementQueryStatementChannel.queueDeclare(rabbitMqProperties.getStatementQueryStatementQueue(), false, true, true, null);
        statementQueryStatementChannel.queueBind(rabbitMqProperties.getStatementQueryStatementQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getStatementQueryStatementQueue());
        statementQueryStatementChannel.basicConsume(rabbitMqProperties.getStatementQueryStatementQueue(), true, this::receivedQueryStatementMessage, consumerTag -> {});
//                .with(rabbitMqProperties.getStatementQueryStatementsRoutingkey())
        Channel statementQueryStatementsChannel = connection.createChannel();
        statementQueryStatementsChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        statementQueryStatementsChannel.queueDeclare(rabbitMqProperties.getStatementQueryStatementsQueue(), false, true, true, null);
        statementQueryStatementsChannel.queueBind(rabbitMqProperties.getStatementQueryStatementsQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getStatementQueryStatementsQueue());
        statementQueryStatementsChannel.basicConsume(rabbitMqProperties.getStatementQueryStatementsQueue(), true, this::receivedQueryStatementsMessage, consumerTag -> {});
//                .with(rabbitMqProperties.getStatementQueryMostRecentStatementRoutingkey())
        Channel statementQueryMostRecentStatementChannel = connection.createChannel();
        statementQueryMostRecentStatementChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        statementQueryMostRecentStatementChannel.queueDeclare(rabbitMqProperties.getStatementQueryMostRecentStatementQueue(), false, true, true, null);
        statementQueryMostRecentStatementChannel.queueBind(rabbitMqProperties.getStatementQueryMostRecentStatementQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getStatementQueryMostRecentStatementQueue());
        statementQueryMostRecentStatementChannel.basicConsume(rabbitMqProperties.getStatementQueryMostRecentStatementQueue(), true, this::receivedQueryMostRecentStatementMessage, consumerTag -> {log.error("CANCEL????");});

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
        MostRecentStatement mostRecentStatement = queryService.findMostRecentStatement(loanId).map(statement -> MostRecentStatement.builder()
                        .id(statement.getId())
                        .loanId(loanId)
                        .statementDate(statement.getStatementDate())
                        .endingBalance(statement.getEndingBalance())
                        .startingBalance(statement.getStartingBalance())
                        .build())
                .orElse(MostRecentStatement.builder().loanId(loanId).build());
        reply(delivery, mostRecentStatement);
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
        responseChannel.basicPublish(rabbitMqProperties.getExchange(), delivery.getProperties().getReplyTo(), replyProps, SerializationUtils.serialize(data));
    }
}
