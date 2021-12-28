package com.github.karlnicholas.merchloan.jms.message;

import com.github.karlnicholas.merchloan.jmsmessage.CreateAccount;
import com.github.karlnicholas.merchloan.jmsmessage.CreditToLoan;
import com.github.karlnicholas.merchloan.jmsmessage.DebitFromLoan;
import com.github.karlnicholas.merchloan.jmsmessage.FundLoan;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RabbitMqSender {
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public RabbitMqSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Value("${rabbitmq.exchange}")
    private String exchange;
    @Value("${rabbitmq.account.createaccount.routingkey}")
    private String accountCreateAccountRoutingkey;
    @Value("${rabbitmq.account.funding.routingkey}")
    private String accountFundingRoutingkey;
    @Value("${rabbitmq.ledger.credittoloan.routingkey}")
    private String ledgerCreditToLoanRoutingkey;
    @Value("${rabbitmq.ledger.debitfromloan.routingkey}")
    private String ledgerDebitFromLoanRoutingkey;
    @Value("${rabbitmq.account.query.account.id.routingkey}")
    private String accountQueryAccountQuerybyidRoutingkey;
    @Value("${rabbitmq.servicerequest.query.id.routingkey}")
    private String servicerequestQueryIdRoutingkey;


    public void sendCreateAccount(CreateAccount createAccount) {
        rabbitTemplate.convertAndSend(exchange, accountCreateAccountRoutingkey, createAccount);
    }

    public void sendFundingRequest(FundLoan fundLoan) {
        rabbitTemplate.convertAndSend(exchange, accountFundingRoutingkey, fundLoan);
    }

    public void sendCreditRequest(CreditToLoan creditToLoan) {
        rabbitTemplate.convertAndSend(exchange, ledgerCreditToLoanRoutingkey, creditToLoan);
    }

    public void sendDebitRequest(DebitFromLoan debitFromLoan) {
        rabbitTemplate.convertAndSend(exchange, ledgerDebitFromLoanRoutingkey, debitFromLoan);
    }

    public void sendDebitFr0mLoan(DebitFromLoan debitFromLoan) {
        rabbitTemplate.convertAndSend(exchange, ledgerDebitFromLoanRoutingkey, debitFromLoan);
    }

    public Object queryServiceRequest(UUID id) {
        return rabbitTemplate.convertSendAndReceive(exchange, servicerequestQueryIdRoutingkey, id);
    }
}