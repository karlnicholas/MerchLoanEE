package com.github.karlnicholas.merchloan.servicerequest.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.karlnicholas.merchloan.dto.RequestStatusDto;
import com.github.karlnicholas.merchloan.jms.config.RabbitMqProperties;
import com.github.karlnicholas.merchloan.servicerequest.message.RabbitMqReceiver;
import com.github.karlnicholas.merchloan.servicerequest.model.ServiceRequest;
import com.github.karlnicholas.merchloan.servicerequest.service.QueryService;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.SerializationUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Configuration
@Slf4j
public class RabbitMQConfig {
    private final RabbitMqProperties rabbitMqProperties;
    private final Channel responseChannel;
    private final QueryService queryService;
    private final ObjectMapper objectMapper;

    public RabbitMQConfig(RabbitMqProperties rabbitMqProperties, ConnectionFactory connectionFactory, RabbitMqReceiver rabbitMqReceiver, QueryService queryService, ObjectMapper objectMapper) throws IOException, TimeoutException {
        this.rabbitMqProperties = rabbitMqProperties;
        this.queryService = queryService;
        this.objectMapper = objectMapper;
        Connection connection = connectionFactory.newConnection();

        Channel serviceRequestReceiveChannel = connection.createChannel();
        serviceRequestReceiveChannel.queueDeclare(rabbitMqProperties.getServicerequestQueue(), false, true, true, null);
        serviceRequestReceiveChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        serviceRequestReceiveChannel.queueBind(rabbitMqProperties.getServicerequestQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getServicerequestQueue());
        serviceRequestReceiveChannel.basicConsume(rabbitMqProperties.getServicerequestQueue(), true, rabbitMqReceiver::receivedServiceRequestMessage, consumerTag -> { });

        serviceRequestReceiveChannel.queueDeclare(rabbitMqProperties.getServicerequestQueryIdQueue(), false, true, true, null);
        serviceRequestReceiveChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        serviceRequestReceiveChannel.queueBind(rabbitMqProperties.getServicerequestQueryIdQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getServicerequestQueryIdQueue());
        serviceRequestReceiveChannel.basicConsume(rabbitMqProperties.getServicerequestQueryIdQueue(), true, this::receivedServiceRequestQueryIdMessage, consumerTag -> { });

        serviceRequestReceiveChannel.queueDeclare(rabbitMqProperties.getServiceRequestCheckRequestQueue(), false, true, true, null);
        serviceRequestReceiveChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        serviceRequestReceiveChannel.queueBind(rabbitMqProperties.getServiceRequestCheckRequestQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getServiceRequestCheckRequestQueue());
        serviceRequestReceiveChannel.basicConsume(rabbitMqProperties.getServiceRequestCheckRequestQueue(), true, this::receivedCheckRequestMessage, consumerTag -> { });

        serviceRequestReceiveChannel.queueDeclare(rabbitMqProperties.getServiceRequestBillLoanQueue(), false, true, true, null);
        serviceRequestReceiveChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        serviceRequestReceiveChannel.queueBind(rabbitMqProperties.getServiceRequestBillLoanQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getServiceRequestBillLoanQueue());
        serviceRequestReceiveChannel.basicConsume(rabbitMqProperties.getServiceRequestBillLoanQueue(), true, rabbitMqReceiver::receivedServiceRequestBillloanMessage, consumerTag -> { });

        serviceRequestReceiveChannel.queueDeclare(rabbitMqProperties.getServiceRequestStatementCompleteQueue(), false, true, true, null);
        serviceRequestReceiveChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        serviceRequestReceiveChannel.queueBind(rabbitMqProperties.getServiceRequestStatementCompleteQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getServiceRequestStatementCompleteQueue());
        serviceRequestReceiveChannel.basicConsume(rabbitMqProperties.getServiceRequestStatementCompleteQueue(), true, rabbitMqReceiver::receivedServiceStatementCompleteMessage, consumerTag -> { });

        connection = connectionFactory.newConnection();
        responseChannel = connection.createChannel();
        responseChannel.queueDeclare(rabbitMqProperties.getServiceRequestResponseQueue(), false, true, true, null);
        responseChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        responseChannel.queueBind(rabbitMqProperties.getServiceRequestResponseQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getServiceRequestResponseQueue());
    }

    public void receivedServiceRequestQueryIdMessage(String consumerTag, Delivery delivery) throws IOException {
        UUID id = (UUID) SerializationUtils.deserialize(delivery.getBody());
        log.debug("ServiceRequestQueryId Received {}", id);
        Optional<ServiceRequest> requestOpt = queryService.getServiceRequest(id);
        String response;
        if (requestOpt.isPresent()) {
            ServiceRequest request = requestOpt.get();
            response = objectMapper.writeValueAsString(RequestStatusDto.builder()
                    .id(request.getId())
                    .localDateTime(request.getLocalDateTime())
                    .status(request.getStatus().name())
                    .statusMessage(request.getStatusMessage())
                    .build());
        } else {
            response = "ERROR: id not found: " + id;
        }
        reply(delivery, response);
    }


    public void receivedCheckRequestMessage(String consumerTag, Delivery delivery) throws IOException {
        log.debug("CheckRequest Received");
        reply(delivery, queryService.checkRequest());
    }

    private void reply(Delivery delivery, Object data) throws IOException {
        AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                .Builder()
                .correlationId(delivery.getProperties().getCorrelationId())
                .build();
        responseChannel.basicPublish(rabbitMqProperties.getExchange(), delivery.getProperties().getReplyTo(), replyProps, SerializationUtils.serialize(data));

    }
}