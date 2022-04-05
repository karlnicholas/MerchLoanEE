package com.github.karlnicholas.merchloan.accounts.message;

import com.github.karlnicholas.merchloan.jms.config.RabbitMqProperties;
import com.github.karlnicholas.merchloan.jmsmessage.ServiceRequestResponse;
import com.github.karlnicholas.merchloan.jmsmessage.StatementHeader;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class RabbitMqSender {
    private final RabbitMqProperties rabbitMqProperties;
    private final Channel serviceRequestChannel;
    private final Channel statementCloseStatementChannel;

    @Autowired
    public RabbitMqSender(ConnectionFactory connectionFactory, RabbitMqProperties rabbitMqProperties) throws IOException, TimeoutException {
        this.rabbitMqProperties = rabbitMqProperties;
        Connection connection = connectionFactory.newConnection();

        serviceRequestChannel = connection.createChannel();
        serviceRequestChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        serviceRequestChannel.queueDeclare(rabbitMqProperties.getServicerequestQueue(), false, false, false, null);
        serviceRequestChannel.exchangeBind(rabbitMqProperties.getServicerequestQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getServicerequestQueue());

        statementCloseStatementChannel = connection.createChannel();
        statementCloseStatementChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        statementCloseStatementChannel.queueDeclare(rabbitMqProperties.getStatementCloseStatementQueue(), false, false, false, null);
        statementCloseStatementChannel.exchangeBind(rabbitMqProperties.getStatementCloseStatementQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getStatementCloseStatementQueue());
    }

    public void serviceRequestServiceRequest(ServiceRequestResponse serviceRequest) throws IOException {
        log.debug("serviceRequestServiceRequest: {}", serviceRequest);
        serviceRequestChannel.basicPublish(rabbitMqProperties.getExchange(), rabbitMqProperties.getServicerequestQueue(), null, SerializationUtils.serialize(serviceRequest));
    }


    public void statementCloseStatement(StatementHeader statementHeader) throws IOException {
        log.debug("statementCloseStatement: {}", statementHeader);
        statementCloseStatementChannel.basicPublish(rabbitMqProperties.getExchange(), rabbitMqProperties.getStatementCloseStatementQueue(), null, SerializationUtils.serialize(statementHeader));
    }


}
