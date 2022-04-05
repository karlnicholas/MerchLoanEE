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
    private final Channel replyChannel;
    private final QueryService queryService;
    private final ObjectMapper objectMapper;

    public RabbitMQConfig(RabbitMqProperties rabbitMqProperties, ConnectionFactory connectionFactory, RabbitMqReceiver rabbitMqReceiver, QueryService queryService, ObjectMapper objectMapper) throws IOException, TimeoutException {
        this.rabbitMqProperties = rabbitMqProperties;
        this.queryService = queryService;
        this.objectMapper = objectMapper;
        Connection connection = connectionFactory.newConnection();

        Channel serviceRequestChannel = connection.createChannel();
        serviceRequestChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        serviceRequestChannel.queueDeclare(rabbitMqProperties.getServicerequestQueue(), false, false, false, null);
        serviceRequestChannel.exchangeBind(rabbitMqProperties.getServicerequestQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getServicerequestQueue());
        serviceRequestChannel.basicConsume(rabbitMqProperties.getServicerequestQueue(), true, rabbitMqReceiver::receivedServiceRequestMessage, consumerTag -> { });

        Channel servicerequestQueryIdChannel = connection.createChannel();
        servicerequestQueryIdChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        servicerequestQueryIdChannel.queueDeclare(rabbitMqProperties.getServicerequestQueryIdQueue(), false, false, false, null);
        servicerequestQueryIdChannel.exchangeBind(rabbitMqProperties.getServicerequestQueryIdQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getServicerequestQueryIdQueue());
        servicerequestQueryIdChannel.basicConsume(rabbitMqProperties.getServicerequestQueryIdQueue(), true, this::receivedServiceRequestQueryIdMessage, consumerTag -> { });

        Channel serviceRequestCheckRequestChannel = connection.createChannel();
        serviceRequestCheckRequestChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        serviceRequestCheckRequestChannel.queueDeclare(rabbitMqProperties.getServiceRequestCheckRequestQueue(), false, false, false, null);
        serviceRequestCheckRequestChannel.exchangeBind(rabbitMqProperties.getServiceRequestCheckRequestQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getServiceRequestCheckRequestQueue());
        serviceRequestCheckRequestChannel.basicConsume(rabbitMqProperties.getServiceRequestCheckRequestQueue(), true, this::receivedCheckRequestMessage, consumerTag -> { });

        Channel servicerequestBillloanChannel = connection.createChannel();
        servicerequestBillloanChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        servicerequestBillloanChannel.queueDeclare(rabbitMqProperties.getServiceRequestBillLoanQueue(), false, false, false, null);
        servicerequestBillloanChannel.exchangeBind(rabbitMqProperties.getServiceRequestBillLoanQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getServiceRequestBillLoanQueue());
        servicerequestBillloanChannel.basicConsume(rabbitMqProperties.getServiceRequestBillLoanQueue(), true, rabbitMqReceiver::receivedServiceRequestBillloanMessage, consumerTag -> { });

        Channel servicerequestStatementCompleteChannel = connection.createChannel();
        servicerequestStatementCompleteChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        servicerequestStatementCompleteChannel.queueDeclare(rabbitMqProperties.getServiceRequestStatementCompleteQueue(), false, false, false, null);
        servicerequestStatementCompleteChannel.exchangeBind(rabbitMqProperties.getServiceRequestStatementCompleteQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getServiceRequestStatementCompleteQueue());
        servicerequestStatementCompleteChannel.basicConsume(rabbitMqProperties.getServiceRequestStatementCompleteQueue(), true, rabbitMqReceiver::receivedServiceStatementCompleteMessage, consumerTag -> { });

        connection = connectionFactory.newConnection();
        replyChannel = connection.createChannel();
        replyChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        replyChannel.queueDeclare(rabbitMqProperties.getServicerequestQueryIdQueue(), false, false, false, null);
        replyChannel.exchangeBind(rabbitMqProperties.getServicerequestQueryIdQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getServicerequestQueryIdQueue());
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
        reply(delivery, response.getBytes(StandardCharsets.UTF_8));
    }


    public void receivedCheckRequestMessage(String consumerTag, Delivery delivery) throws IOException {
        log.debug("CheckRequest Received");
        reply(delivery, queryService.checkRequest().toString().getBytes(StandardCharsets.UTF_8));
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