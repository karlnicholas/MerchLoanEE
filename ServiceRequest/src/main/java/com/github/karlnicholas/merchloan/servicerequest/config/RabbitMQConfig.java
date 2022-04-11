package com.github.karlnicholas.merchloan.servicerequest.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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

    public RabbitMQConfig(RabbitMqProperties rabbitMqProperties, Connection connection, RabbitMqReceiver rabbitMqReceiver, QueryService queryService) throws IOException, TimeoutException {
        this.rabbitMqProperties = rabbitMqProperties;
        this.queryService = queryService;
        this.objectMapper = new ObjectMapper().findAndRegisterModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        Channel servicerequestChannel = connection.createChannel();
        servicerequestChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        servicerequestChannel.queueDeclare(rabbitMqProperties.getServicerequestQueue(), false, true, true, null);
        servicerequestChannel.queueBind(rabbitMqProperties.getServicerequestQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getServicerequestQueue());
        servicerequestChannel.basicConsume(rabbitMqProperties.getServicerequestQueue(), true, rabbitMqReceiver::receivedServiceRequestMessage, consumerTag -> { });

        Channel servicerequestQueryIdChannel = connection.createChannel();
        servicerequestQueryIdChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        servicerequestQueryIdChannel.queueDeclare(rabbitMqProperties.getServicerequestQueryIdQueue(), false, true, true, null);
        servicerequestQueryIdChannel.queueBind(rabbitMqProperties.getServicerequestQueryIdQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getServicerequestQueryIdQueue());
        servicerequestQueryIdChannel.basicConsume(rabbitMqProperties.getServicerequestQueryIdQueue(), true, this::receivedServiceRequestQueryIdMessage, consumerTag -> { });

        Channel serviceRequestCheckRequestChannel = connection.createChannel();
        serviceRequestCheckRequestChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        serviceRequestCheckRequestChannel.queueDeclare(rabbitMqProperties.getServiceRequestCheckRequestQueue(), false, true, true, null);
        serviceRequestCheckRequestChannel.queueBind(rabbitMqProperties.getServiceRequestCheckRequestQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getServiceRequestCheckRequestQueue());
        serviceRequestCheckRequestChannel.basicConsume(rabbitMqProperties.getServiceRequestCheckRequestQueue(), true, this::receivedCheckRequestMessage, consumerTag -> { });

        Channel serviceRequestBillLoanChannel = connection.createChannel();
        serviceRequestBillLoanChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        serviceRequestBillLoanChannel.queueDeclare(rabbitMqProperties.getServiceRequestBillLoanQueue(), false, true, true, null);
        serviceRequestBillLoanChannel.queueBind(rabbitMqProperties.getServiceRequestBillLoanQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getServiceRequestBillLoanQueue());
        serviceRequestBillLoanChannel.basicConsume(rabbitMqProperties.getServiceRequestBillLoanQueue(), true, rabbitMqReceiver::receivedServiceRequestBillloanMessage, consumerTag -> { });

        Channel serviceRequestStatementCompleteQueue = connection.createChannel();
        serviceRequestStatementCompleteQueue.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
        serviceRequestStatementCompleteQueue.queueDeclare(rabbitMqProperties.getServiceRequestStatementCompleteQueue(), false, true, true, null);
        serviceRequestStatementCompleteQueue.queueBind(rabbitMqProperties.getServiceRequestStatementCompleteQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getServiceRequestStatementCompleteQueue());
        serviceRequestStatementCompleteQueue.basicConsume(rabbitMqProperties.getServiceRequestStatementCompleteQueue(), true, rabbitMqReceiver::receivedServiceStatementCompleteMessage, consumerTag -> { });

        responseChannel = connection.createChannel();
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