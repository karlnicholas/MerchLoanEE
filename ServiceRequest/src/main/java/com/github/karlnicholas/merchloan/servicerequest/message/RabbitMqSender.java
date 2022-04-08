package com.github.karlnicholas.merchloan.servicerequest.message;

import com.github.karlnicholas.merchloan.jms.config.RabbitMqProperties;
import com.github.karlnicholas.merchloan.jmsmessage.*;
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
    private final Channel serviceRequestSenderChannel;

    @Autowired
    public RabbitMqSender(ConnectionFactory connectionFactory, RabbitMqProperties rabbitMqProperties) throws IOException, TimeoutException {
        this.rabbitMqProperties = rabbitMqProperties;
        Connection connection = connectionFactory.newConnection();
        serviceRequestSenderChannel = connection.createChannel();
    }

    public void accountCreateAccount(CreateAccount createAccount) throws IOException {
        log.debug("accountCreateAccount: {}", createAccount);
        serviceRequestSenderChannel.basicPublish(rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountCreateaccountQueue(), null, SerializationUtils.serialize(createAccount));
    }

    public void accountFundLoan(FundLoan fundLoan) throws IOException {
        log.debug("accountFundLoan: {}", fundLoan);
        serviceRequestSenderChannel.basicPublish(rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountFundingQueue(), null, SerializationUtils.serialize(fundLoan));
    }

    public void accountValidateCredit(CreditLoan creditLoan) throws IOException {
        log.debug("accountValidateCredit: {}", creditLoan);
        serviceRequestSenderChannel.basicPublish(rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountValidateCreditQueue(), null, SerializationUtils.serialize(creditLoan));
    }

    public void accountValidateDebit(DebitLoan debitLoan) throws IOException {
        log.debug("accountValidateDebit: {}", debitLoan);
        serviceRequestSenderChannel.basicPublish(rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountValidateDebitQueue(), null, SerializationUtils.serialize(debitLoan));
    }

    public void statementStatement(StatementHeader statementHeader) throws IOException {
        log.debug("statementStatement: {}", statementHeader);
        serviceRequestSenderChannel.basicPublish(rabbitMqProperties.getExchange(), rabbitMqProperties.getStatementStatementQueue(), null, SerializationUtils.serialize(statementHeader));
    }

    public void accountCloseLoan(CloseLoan closeLoan) throws IOException {
        log.debug("accountCloseLoan: {}", closeLoan);
        serviceRequestSenderChannel.basicPublish(rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountCloseLoanQueue(), null, SerializationUtils.serialize(closeLoan));
    }

}
