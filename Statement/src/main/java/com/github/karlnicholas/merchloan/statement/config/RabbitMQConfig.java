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
    private Channel statementReceiveChannel;
    private Channel responseChannel;
    private final ObjectMapper objectMapper;

    public RabbitMQConfig(RabbitMqProperties rabbitMqProperties, RabbitMqReceiver rabbitMqReceiver, QueryService queryService, ConnectionFactory connectionFactory) throws IOException, TimeoutException {
        this.rabbitMqProperties = rabbitMqProperties;
        this.queryService = queryService;
        this.objectMapper = new ObjectMapper().findAndRegisterModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        Connection connection = connectionFactory.newConnection();
        statementReceiveChannel = connection.createChannel();
        statementReceiveChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);

//                .with(rabbitMqProperties.getStatementStatementRoutingkey())
        statementReceiveChannel.queueDeclare(rabbitMqProperties.getStatementStatementQueue(), false, true, true, null);
        statementReceiveChannel.queueBind(rabbitMqProperties.getStatementStatementQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getStatementStatementQueue());
        statementReceiveChannel.basicConsume(rabbitMqProperties.getStatementStatementQueue(), true, rabbitMqReceiver::receivedStatementMessage, consumerTag -> {
        });
//                .with(rabbitMqProperties.getStatementCloseStatementRoutingkey())
        statementReceiveChannel.queueDeclare(rabbitMqProperties.getStatementCloseStatementQueue(), false, true, true, null);
        statementReceiveChannel.queueBind(rabbitMqProperties.getStatementCloseStatementQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getStatementCloseStatementQueue());
        statementReceiveChannel.basicConsume(rabbitMqProperties.getStatementCloseStatementQueue(), true, rabbitMqReceiver::receivedCloseStatementMessage, consumerTag -> {
        });
//                .with(rabbitMqProperties.getStatementQueryStatementRoutingkey())
        statementReceiveChannel.queueDeclare(rabbitMqProperties.getStatementQueryStatementQueue(), false, true, true, null);
        statementReceiveChannel.queueBind(rabbitMqProperties.getStatementQueryStatementQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getStatementQueryStatementQueue());
        statementReceiveChannel.basicConsume(rabbitMqProperties.getStatementQueryStatementQueue(), true, this::receivedQueryStatementMessage, consumerTag -> {
        });
//                .with(rabbitMqProperties.getStatementQueryStatementsRoutingkey())
        statementReceiveChannel.queueDeclare(rabbitMqProperties.getStatementQueryStatementsQueue(), false, true, true, null);
        statementReceiveChannel.queueBind(rabbitMqProperties.getStatementQueryStatementsQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getStatementQueryStatementsQueue());
        statementReceiveChannel.basicConsume(rabbitMqProperties.getStatementQueryStatementsQueue(), true, this::receivedQueryStatementsMessage, consumerTag -> {
        });
//                .with(rabbitMqProperties.getStatementQueryMostRecentStatementRoutingkey())
        statementReceiveChannel.queueDeclare(rabbitMqProperties.getStatementQueryMostRecentStatementQueue(), false, true, true, null);
        statementReceiveChannel.queueBind(rabbitMqProperties.getStatementQueryMostRecentStatementQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getStatementQueryMostRecentStatementQueue());
        statementReceiveChannel.basicConsume(rabbitMqProperties.getStatementQueryMostRecentStatementQueue(), true, this::receivedQueryMostRecentStatementMessage, consumerTag -> {
        });

        connection = connectionFactory.newConnection();
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
        log.info("receivedQueryMostRecentStatementMessage {}", loanId);
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
