package com.github.karlnicholas.merchloan.ledger.message;

import com.github.karlnicholas.merchloan.jmsmessage.CreditToLoan;
import com.github.karlnicholas.merchloan.jmsmessage.DebitFromLoan;
import com.github.karlnicholas.merchloan.ledger.service.LedgerManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.stereotype.Component;

@Component
public class RabbitMqReceiver implements RabbitListenerConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(RabbitMqReceiver.class);
    private final LedgerManagementService ledgerManagementService;

    public RabbitMqReceiver(LedgerManagementService ledgerManagementService) {
        this.ledgerManagementService = ledgerManagementService;
    }

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar rabbitListenerEndpointRegistrar) {
    }

    @RabbitListener(queues = "${rabbitmq.ledger.credittoloan.queue}")
    public void receivedCreditMessage(CreditToLoan creditToLoan) {
        logger.info("CreditToLoan Details Received is.. " + creditToLoan);
        ledgerManagementService.creditToLoan(creditToLoan);
    }

    @RabbitListener(queues = "${rabbitmq.ledger.debitfromloan.queue}")
    public void receivedDebitMessage(DebitFromLoan debitFromLoan) {
        logger.info("DebitFromLoan Details Received is.. " + debitFromLoan);
        ledgerManagementService.debitFromLoan(debitFromLoan);
    }
}