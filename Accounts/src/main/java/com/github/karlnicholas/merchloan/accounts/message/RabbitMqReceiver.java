package com.github.karlnicholas.merchloan.accounts.message;

import com.github.karlnicholas.merchloan.jmsmessage.CreateAccount;
import com.github.karlnicholas.merchloan.accounts.service.AccountManagementService;
import com.github.karlnicholas.merchloan.jmsmessage.FundLoan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.stereotype.Component;

@Component
public class RabbitMqReceiver implements RabbitListenerConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(RabbitMqReceiver.class);
    private final AccountManagementService accountManagementService;

    public RabbitMqReceiver(AccountManagementService accountManagementService) {
        this.accountManagementService = accountManagementService;
    }

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar rabbitListenerEndpointRegistrar) {
    }

    @RabbitListener(queues = "${rabbitmq.account.createaccount.queue}")
    public void receivedAccountMessage(CreateAccount createAccount) {
        logger.info("CreateAccount Details Received is.. " + createAccount);
        accountManagementService.createAccount(createAccount);
    }

    @RabbitListener(queues = "${rabbitmq.account.funding.queue}")
    public void receivedFundingMessage(FundLoan funding) {
        logger.info("FundLoan Details Received is.. " + funding);
        accountManagementService.fundAccount(funding);
    }

}