package com.github.karlnicholas.merchloan.statement.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Configuration
@Slf4j
public class RabbitMQConfig {
    private final RabbitMqProperties rabbitMqProperties;
    private final ObjectMapper objectMapper;
    private final QueryService queryService;
    private Channel statementStatementQueue;
    private Channel statementCloseStatementQueue;
    private Channel statementQueryStatementQueue;
    private Channel statementQueryStatementsQueue;
    private Channel statementQueryMostRecentStatementQueue;
    private Channel replyChannel;

    public RabbitMQConfig(RabbitMqProperties rabbitMqProperties, RabbitMqReceiver rabbitMqReceiver, ObjectMapper objectMapper, QueryService queryService, ConnectionFactory connectionFactory) throws IOException, TimeoutException {
        this.rabbitMqProperties = rabbitMqProperties;
        this.objectMapper = objectMapper;
        this.queryService = queryService;
        Connection connection = connectionFactory.newConnection();

//                .with(rabbitMqProperties.getStatementStatementRoutingkey())
        statementStatementQueue = connection.createChannel();
        statementStatementQueue.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        statementStatementQueue.queueDeclare(rabbitMqProperties.getStatementStatementQueue(), false, false, false, null);
        statementStatementQueue.exchangeBind(rabbitMqProperties.getStatementStatementQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getStatementStatementQueue());
        statementStatementQueue.basicConsume(rabbitMqProperties.getStatementStatementQueue(), true, rabbitMqReceiver::receivedStatementMessage, consumerTag -> {});
//                .with(rabbitMqProperties.getStatementCloseStatementRoutingkey())
        statementCloseStatementQueue = connection.createChannel();
        statementCloseStatementQueue.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        statementCloseStatementQueue.queueDeclare(rabbitMqProperties.getStatementCloseStatementQueue(), false, false, false, null);
        statementCloseStatementQueue.exchangeBind(rabbitMqProperties.getStatementCloseStatementQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getStatementCloseStatementQueue());
        statementCloseStatementQueue.basicConsume(rabbitMqProperties.getStatementCloseStatementQueue(), true, rabbitMqReceiver::receivedCloseStatementMessage, consumerTag -> {});
//                .with(rabbitMqProperties.getStatementQueryStatementRoutingkey())
        statementQueryStatementQueue = connection.createChannel();
        statementQueryStatementQueue.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        statementQueryStatementQueue.queueDeclare(rabbitMqProperties.getStatementQueryStatementQueue(), false, false, false, null);
        statementQueryStatementQueue.exchangeBind(rabbitMqProperties.getStatementQueryStatementQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getStatementQueryStatementQueue());
        statementQueryStatementQueue.basicConsume(rabbitMqProperties.getStatementQueryStatementQueue(), true, this::receivedQueryStatementMessage, consumerTag -> {});
//                .with(rabbitMqProperties.getStatementQueryStatementsRoutingkey())
        statementQueryStatementsQueue = connection.createChannel();
        statementQueryStatementsQueue.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        statementQueryStatementsQueue.queueDeclare(rabbitMqProperties.getStatementQueryStatementsQueue(), false, false, false, null);
        statementQueryStatementsQueue.exchangeBind(rabbitMqProperties.getStatementQueryStatementsQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getStatementQueryStatementsQueue());
        statementQueryStatementsQueue.basicConsume(rabbitMqProperties.getStatementQueryStatementsQueue(), true, this::receivedQueryStatementsMessage, consumerTag -> {});
//                .with(rabbitMqProperties.getStatementQueryMostRecentStatementRoutingkey())
        statementQueryMostRecentStatementQueue = connection.createChannel();
        statementQueryMostRecentStatementQueue.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        statementQueryMostRecentStatementQueue.queueDeclare(rabbitMqProperties.getStatementQueryMostRecentStatementQueue(), false, false, false, null);
        statementQueryMostRecentStatementQueue.exchangeBind(rabbitMqProperties.getStatementQueryMostRecentStatementQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getStatementQueryMostRecentStatementQueue());
        statementQueryMostRecentStatementQueue.basicConsume(rabbitMqProperties.getStatementQueryMostRecentStatementQueue(), true, this::receivedQueryMostRecentStatementMessage, consumerTag -> {});

        connection = connectionFactory.newConnection();
        replyChannel = connection.createChannel();
        replyChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        replyChannel.queueDeclare(rabbitMqProperties.getServicerequestQueryIdQueue(), false, false, false, null);
        replyChannel.exchangeBind(rabbitMqProperties.getServicerequestQueryIdQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getServicerequestQueryIdQueue());
    }

    public void receivedQueryStatementMessage(String consumerTag, Delivery delivery) throws IOException {
        UUID loanId = (UUID) SerializationUtils.deserialize(delivery.getBody());
        log.debug("receivedQueryStatementMessage {}", loanId);
        String result = queryService.findById(loanId).map(Statement::getStatement).orElse("ERROR: No statement found for id " + loanId);
        reply(delivery, result.getBytes(StandardCharsets.UTF_8));
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
                .build()).orElse(MostRecentStatement.builder().loanId(loanId).build());
        reply(delivery, objectMapper.writeValueAsString(mostRecentStatement).getBytes(StandardCharsets.UTF_8));
    }

    public void receivedQueryStatementsMessage(String consumerTag, Delivery delivery) throws IOException {
        UUID id = (UUID) SerializationUtils.deserialize(delivery.getBody());
        log.debug("receivedQueryStatementsMessage Received {}", id);
        String result = objectMapper.writeValueAsString(queryService.findByLoanId(id));
        reply(delivery, result.getBytes(StandardCharsets.UTF_8));
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
