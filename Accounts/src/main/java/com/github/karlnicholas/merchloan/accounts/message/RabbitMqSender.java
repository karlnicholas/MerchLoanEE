package com.github.karlnicholas.merchloan.accounts.message;

import com.github.karlnicholas.merchloan.jmsmessage.CreditToLoan;
import com.github.karlnicholas.merchloan.jmsmessage.DebitFromLoan;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RabbitMqSender {
    private RabbitTemplate rabbitTemplate;

    @Autowired
    public RabbitMqSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Value("${rabbitmq.exchange}")
    private String exchange;
    @Value("${rabbitmq.ledger.debitfromloan.routingkey}")
    private String ledgerDebitFromLoanRoutingkey;
    @Value("${rabbitmq.ledger.credittoloan.routingkey}")
    private String ledgerCreditToLoanRoutingkey;


    public void sendDebitFr0mLoan(DebitFromLoan debitFromLoan) {
        rabbitTemplate.convertAndSend(exchange, ledgerDebitFromLoanRoutingkey, debitFromLoan);
    }

}