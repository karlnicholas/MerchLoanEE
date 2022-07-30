package com.github.karlnicholas.merchloan.accounts.message;

import com.github.karlnicholas.merchloan.accounts.service.AccountManagementService;
import com.github.karlnicholas.merchloan.accounts.service.RegisterManagementService;
import com.github.karlnicholas.merchloan.jmsmessage.DebitLoan;
import com.github.karlnicholas.merchloan.jmsmessage.FundLoan;
import com.github.karlnicholas.merchloan.jmsmessage.ServiceRequestResponse;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.*;

@MessageDriven(name = "AccountFundingMDB", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/AccountsFundingQueue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
@Slf4j
public class AccountFundingListener implements MessageListener {
    @Inject
    private JMSContext jmsContext;
    @Resource(lookup = "java:global/jms/queue/ServiceRequestResponseQueue")
    private Destination serviceRequestQueue;
    @Inject
    private AccountManagementService accountManagementService;
    @Inject
    private RegisterManagementService registerManagementService;

    @Override
    public void onMessage(Message message) {
        // M= P [r (1+r)^n/ ((1+r)^n)-1)]
        // r = .10 / 12 = 0.00833
        // 10000 * 0.00833(1.00833)^12 / ((1.00833)^12)-1]
        // 10000 * 0.0092059/0.104713067
        // 10000 * 0.08791548
        // = 879.16
        FundLoan fundLoan = null;
        try {
            fundLoan = (FundLoan) ((ObjectMessage) message).getObject();
        } catch (JMSException e) {
            log.error("receivedFundingMessage exception", e);
            return;
        }
        ServiceRequestResponse requestResponse = ServiceRequestResponse.builder()
                .id(fundLoan.getId())
                .build();
        try {
            log.debug("receivedFundingMessage: {} ", fundLoan);
            accountManagementService.fundAccount(fundLoan, requestResponse);
            if (requestResponse.isSuccess()) {
                registerManagementService.fundLoan(
                        DebitLoan.builder()
                                .id(fundLoan.getId())
                                .amount(fundLoan.getAmount())
                                .date(fundLoan.getStartDate())
                                .loanId(fundLoan.getId())
                                .description(fundLoan.getDescription())
                                .build(),
                        requestResponse);
            }
        } catch (Exception ex) {
            log.error("receivedFundingMessage exception", ex);
            requestResponse.setError(ex.getMessage());
        } finally {
            jmsContext.createProducer().setDeliveryMode(DeliveryMode.NON_PERSISTENT).send(serviceRequestQueue, jmsContext.createObjectMessage(requestResponse));
        }

    }
}
