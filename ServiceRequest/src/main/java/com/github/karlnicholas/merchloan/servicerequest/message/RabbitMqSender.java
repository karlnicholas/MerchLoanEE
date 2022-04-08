package com.github.karlnicholas.merchloan.servicerequest.message;

import com.github.karlnicholas.merchloan.jms.config.RabbitMqProperties;
import com.github.karlnicholas.merchloan.jmsmessage.*;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class RabbitMqSender {
    private final RabbitMqProperties rabbitMqProperties;
    private final Channel serviceRequestSenderChannel;
//    private final Channel accountFundLoanChannel;
//    private final Channel accountValidateCreditChannel;
//    private final Channel accountValidateDebitChannel;
//    private final Channel statementStatementChannel;
//    private final Channel accountCloseLoanChannel;

    @Autowired
    public RabbitMqSender(ConnectionFactory connectionFactory, RabbitMqProperties rabbitMqProperties) throws IOException, TimeoutException {
        this.rabbitMqProperties = rabbitMqProperties;
        Connection connection = connectionFactory.newConnection();
        serviceRequestSenderChannel = connection.createChannel();
//        serviceRequestSenderChannel.queueDeclare(rabbitMqProperties.getAccountCreateaccountQueue(), false, true, true, null);
//        serviceRequestSenderChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
//        serviceRequestSenderChannel.queueBind(rabbitMqProperties.getAccountCreateaccountQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountCreateaccountQueue());
//
//        serviceRequestSenderChannel.queueDeclare(rabbitMqProperties.getAccountFundingQueue(), false, true, true, null);
//        serviceRequestSenderChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
//        serviceRequestSenderChannel.queueBind(rabbitMqProperties.getAccountFundingQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountFundingQueue());
//
//        serviceRequestSenderChannel.queueDeclare(rabbitMqProperties.getAccountValidateCreditQueue(), false, true, true, null);
//        serviceRequestSenderChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
//        serviceRequestSenderChannel.queueBind(rabbitMqProperties.getAccountValidateCreditQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountValidateCreditQueue());
//
//        serviceRequestSenderChannel.queueDeclare(rabbitMqProperties.getAccountValidateDebitQueue(), false, true, true, null);
//        serviceRequestSenderChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
//        serviceRequestSenderChannel.queueBind(rabbitMqProperties.getAccountValidateDebitQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountValidateDebitQueue());
//
//        serviceRequestSenderChannel.queueDeclare(rabbitMqProperties.getStatementStatementQueue(), false, true, true, null);
//        serviceRequestSenderChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
//        serviceRequestSenderChannel.queueBind(rabbitMqProperties.getStatementStatementQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getStatementStatementQueue());
//
//        serviceRequestSenderChannel.queueDeclare(rabbitMqProperties.getAccountCloseLoanQueue(), false, true, true, null);
//        serviceRequestSenderChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);
//        serviceRequestSenderChannel.queueBind(rabbitMqProperties.getAccountCloseLoanQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountCloseLoanQueue());
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
