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
    private final Channel accountCreateAccountChannel;
    private final Channel accountFundLoanChannel;
    private final Channel accountValidateCreditChannel;
    private final Channel accountValidateDebitChannel;
    private final Channel statementStatementChannel;
    private final Channel accountCloseLoanChannel;

    @Autowired
    public RabbitMqSender(ConnectionFactory connectionFactory, RabbitMqProperties rabbitMqProperties) throws IOException, TimeoutException {
        this.rabbitMqProperties = rabbitMqProperties;
        Connection connection = connectionFactory.newConnection();
        accountCreateAccountChannel = connection.createChannel();
        accountCreateAccountChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        accountCreateAccountChannel.queueDeclare(rabbitMqProperties.getAccountCreateaccountQueue(), false, false, false, null);
        accountCreateAccountChannel.exchangeBind(rabbitMqProperties.getAccountCreateaccountQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountCreateaccountQueue());

        accountFundLoanChannel = connection.createChannel();
        accountFundLoanChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        accountFundLoanChannel.queueDeclare(rabbitMqProperties.getAccountFundingQueue(), false, false, false, null);
        accountFundLoanChannel.exchangeBind(rabbitMqProperties.getAccountFundingQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountFundingQueue());

        accountValidateCreditChannel = connection.createChannel();
        accountValidateCreditChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        accountValidateCreditChannel.queueDeclare(rabbitMqProperties.getAccountValidateCreditQueue(), false, false, false, null);
        accountValidateCreditChannel.exchangeBind(rabbitMqProperties.getAccountValidateCreditQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountValidateCreditQueue());

        accountValidateDebitChannel = connection.createChannel();
        accountValidateDebitChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        accountValidateDebitChannel.queueDeclare(rabbitMqProperties.getAccountValidateDebitQueue(), false, false, false, null);
        accountValidateDebitChannel.exchangeBind(rabbitMqProperties.getAccountValidateDebitQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountValidateDebitQueue());

        statementStatementChannel = connection.createChannel();
        statementStatementChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        statementStatementChannel.queueDeclare(rabbitMqProperties.getStatementStatementQueue(), false, false, false, null);
        statementStatementChannel.exchangeBind(rabbitMqProperties.getStatementStatementQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getStatementStatementQueue());

        accountCloseLoanChannel = connection.createChannel();
        accountCloseLoanChannel.exchangeDeclare(rabbitMqProperties.getExchange(), BuiltinExchangeType.DIRECT);
        accountCloseLoanChannel.queueDeclare(rabbitMqProperties.getAccountCloseLoanQueue(), false, false, false, null);
        accountCloseLoanChannel.exchangeBind(rabbitMqProperties.getAccountCloseLoanQueue(), rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountCloseLoanQueue());
    }

    public void accountCreateAccount(CreateAccount createAccount) throws IOException {
        log.debug("accountCreateAccount: {}", createAccount);
        accountCreateAccountChannel.basicPublish(rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountCreateaccountQueue(), null, SerializationUtils.serialize(createAccount));
    }

    public void accountFundLoan(FundLoan fundLoan) throws IOException {
        log.debug("accountFundLoan: {}", fundLoan);
        accountFundLoanChannel.basicPublish(rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountFundingQueue(), null, SerializationUtils.serialize(fundLoan));
    }

    public void accountValidateCredit(CreditLoan creditLoan) throws IOException {
        log.debug("accountValidateCredit: {}", creditLoan);
        accountValidateCreditChannel.basicPublish(rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountValidateCreditQueue(), null, SerializationUtils.serialize(creditLoan));
    }

    public void accountValidateDebit(DebitLoan debitLoan) throws IOException {
        log.debug("accountValidateDebit: {}", debitLoan);
        accountValidateDebitChannel.basicPublish(rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountValidateDebitQueue(), null, SerializationUtils.serialize(debitLoan));
    }

    public void statementStatement(StatementHeader statementHeader) throws IOException {
        log.debug("statementStatement: {}", statementHeader);
        statementStatementChannel.basicPublish(rabbitMqProperties.getExchange(), rabbitMqProperties.getStatementStatementQueue(), null, SerializationUtils.serialize(statementHeader));
    }

    public void accountCloseLoan(CloseLoan closeLoan) throws IOException {
        log.debug("accountCloseLoan: {}", closeLoan);
        accountCloseLoanChannel.basicPublish(rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountCloseLoanQueue(), null, SerializationUtils.serialize(closeLoan));
    }
}
