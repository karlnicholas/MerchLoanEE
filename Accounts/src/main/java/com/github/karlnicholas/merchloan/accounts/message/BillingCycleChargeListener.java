package com.github.karlnicholas.merchloan.accounts.message;

import com.github.karlnicholas.merchloan.accounts.model.RegisterEntry;
import com.github.karlnicholas.merchloan.accounts.service.RegisterManagementService;
import com.github.karlnicholas.merchloan.jmsmessage.BillingCycleCharge;
import com.github.karlnicholas.merchloan.jmsmessage.RegisterEntryMessage;
import lombok.extern.slf4j.Slf4j;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.DeliveryMode;
import javax.jms.JMSContext;
import javax.jms.Message;
import javax.jms.MessageListener;

@MessageDriven(name = "BillingCycleChargeMDB", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/AccountsBillingCycleChargeQueue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
@Slf4j
public class BillingCycleChargeListener implements MessageListener {
    @Inject
    private JMSContext jmsContext;
    @Inject
    private RegisterManagementService registerManagementService;
    @Override
    public void onMessage(Message message) {
        try {
            BillingCycleCharge billingCycleCharge = message.getBody(BillingCycleCharge.class);
            log.debug("receivedBillingCycleChargeMessage: {}", billingCycleCharge);
            RegisterEntry re = registerManagementService.billingCycleCharge(billingCycleCharge);
            jmsContext.createProducer()
                    .setDeliveryMode(DeliveryMode.NON_PERSISTENT)
                    .setJMSCorrelationID(message.getJMSCorrelationID())
                    .send(message.getJMSReplyTo(), jmsContext.createObjectMessage(RegisterEntryMessage.builder()
                            .date(re.getDate())
                            .credit(re.getCredit())
                            .debit(re.getDebit())
                            .description(re.getDescription())
                            .timeStamp(re.getTimeStamp())
                            .build()));
        } catch (Exception ex) {
            log.error("receivedBillingCycleChargeMessage exception {}", ex.getMessage());
            throw new EJBException(ex);
        }
    }
}
