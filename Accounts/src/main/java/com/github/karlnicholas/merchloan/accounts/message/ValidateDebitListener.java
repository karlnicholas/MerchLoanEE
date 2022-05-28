package com.github.karlnicholas.merchloan.accounts.message;

import com.github.karlnicholas.merchloan.accounts.service.AccountManagementService;
import com.github.karlnicholas.merchloan.accounts.service.RegisterManagementService;
import com.github.karlnicholas.merchloan.jmsmessage.DebitLoan;
import com.github.karlnicholas.merchloan.jmsmessage.ServiceRequestResponse;
import lombok.extern.slf4j.Slf4j;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.*;

@MessageDriven(name = "ValidateDebitMDB", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/AccountValidateDebitQueue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
@Slf4j
public class ValidateDebitListener implements MessageListener {
    @Inject
    private AccountManagementService accountManagementService;
    @Inject
    private RegisterManagementService registerManagementService;
    @Inject
    private MQProducers mqProducers;

    @Override
    public void onMessage(Message message) {
        DebitLoan debitLoan = null;
        try {
            debitLoan = (DebitLoan) ((ObjectMessage) message).getObject();
        } catch (JMSException e) {
            log.error("receivedValidateDebitMessage exception", e);
            throw new EJBException(e);
        }
        ServiceRequestResponse requestResponse = ServiceRequestResponse.builder()
                .id(debitLoan.getId())
                .build();
        try {
            log.debug("receivedValidateDebitMessage: {} ", debitLoan);
            accountManagementService.validateLoan(debitLoan.getLoanId(), requestResponse);
            if (requestResponse.isSuccess()) {
                registerManagementService.debitLoan(DebitLoan.builder()
                                .id(debitLoan.getId())
                                .amount(debitLoan.getAmount())
                                .date(debitLoan.getDate())
                                .loanId(debitLoan.getLoanId())
                                .description(debitLoan.getDescription())
                                .build(),
                        requestResponse);
            }
        } catch (Exception ex) {
            log.error("receivedValidateDebitMessage exception", ex);
            requestResponse.setError(ex.getMessage());
        } finally {
            mqProducers.serviceRequestServiceRequest(requestResponse);
        }
    }
}
