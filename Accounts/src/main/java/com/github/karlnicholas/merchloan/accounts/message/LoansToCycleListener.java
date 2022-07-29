package com.github.karlnicholas.merchloan.accounts.message;

import com.github.karlnicholas.merchloan.accounts.service.AccountManagementService;
import com.github.karlnicholas.merchloan.jmsmessage.BillingCycle;
import lombok.extern.slf4j.Slf4j;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.DeliveryMode;
import javax.jms.JMSContext;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@MessageDriven(name = "LoansToCycleMDB", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/AccountLoansToCycleQueue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
@Slf4j
public class LoansToCycleListener implements MessageListener {
    @Inject
    private JMSContext jmsContext;
    @Inject
    private AccountManagementService accountManagementService;

    @Override
    public void onMessage(Message message) {
        try {
            LocalDate businessDate = message.getBody(LocalDate.class);
            log.trace("receivedLoansToCycleMessage: {}", businessDate);
            ArrayList<BillingCycle> loansToCycle = accountManagementService.loansToCycle(businessDate);
            jmsContext.createProducer()
                    .setDeliveryMode(DeliveryMode.NON_PERSISTENT)
                    .setJMSCorrelationID(message.getJMSCorrelationID())
                    .send(message.getJMSReplyTo(), jmsContext.createObjectMessage(loansToCycle));
        } catch (Exception ex) {
            log.error("receivedLoansToCycleMessage exception {}", ex.getMessage());
            throw new EJBException(ex);
        }
    }
}
