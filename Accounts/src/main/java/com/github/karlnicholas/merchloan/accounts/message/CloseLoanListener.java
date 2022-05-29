package com.github.karlnicholas.merchloan.accounts.message;

import com.github.karlnicholas.merchloan.accounts.service.QueryService;
import com.github.karlnicholas.merchloan.accounts.service.RegisterManagementService;
import com.github.karlnicholas.merchloan.dto.LoanDto;
import com.github.karlnicholas.merchloan.jmsmessage.*;
import lombok.extern.slf4j.Slf4j;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.sql.SQLException;
import java.util.Optional;

@MessageDriven(name = "CloseLoanMDB", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/AccountCloseLoanQueue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
@Slf4j
public class CloseLoanListener implements MessageListener {
    @Inject
    private QueryService queryService;
    @Inject
    private RegisterManagementService registerManagementService;
    @Inject
    private MQProducers mqProducers;
    @Override
    public void onMessage(Message message) {
        CloseLoan closeLoan = null;
        try {
            closeLoan = (CloseLoan) ((ObjectMessage) message).getObject();
        } catch (JMSException e) {
            log.error("receivedFundingMessage exception", e);
            return;
        }
        ServiceRequestResponse serviceRequestResponse = ServiceRequestResponse.builder().id(closeLoan.getId()).build();
        try {
            log.debug("receivedCloseLoanMessage {} ", closeLoan);
            Optional<LoanDto> loanOpt = queryService.queryLoanId(closeLoan.getLoanId());
            if (loanOpt.isPresent()) {
                if (closeLoan.getAmount().compareTo(loanOpt.get().getPayoffAmount()) == 0) {
                    registerManagementService.debitLoan(DebitLoan.builder()
                                    .id(closeLoan.getInterestChargeId())
                                    .loanId(closeLoan.getLoanId())
                                    .date(closeLoan.getDate())
                                    .amount(loanOpt.get().getCurrentInterest())
                                    .description("Interest")
                                    .build()
                            , serviceRequestResponse);
                    // determine interest balance
                    registerManagementService.creditLoan(CreditLoan.builder()
                                    .id(closeLoan.getPaymentId())
                                    .loanId(closeLoan.getLoanId())
                                    .date(closeLoan.getDate())
                                    .amount(closeLoan.getAmount())
                                    .description("Payoff Payment")
                                    .build()
                            , serviceRequestResponse);
                    closeLoan.setLoanDto(loanOpt.get());
                    closeLoan.setLastStatementDate(loanOpt.get().getLastStatementDate());
                    StatementHeader statementHeader = StatementHeader.builder()
                            .id(closeLoan.getId())
                            .accountId(closeLoan.getLoanDto().getAccountId())
                            .loanId(closeLoan.getLoanId())
                            .statementDate(closeLoan.getDate())
                            .startDate(closeLoan.getLastStatementDate().plusDays(1))
                            .endDate(closeLoan.getDate())
                            .build();
                    registerManagementService.setStatementHeaderRegisterEntryies(statementHeader);
                    mqProducers.statementCloseStatement(statementHeader);
                } else {
                    serviceRequestResponse.setFailure("PayoffAmount incorrect. Required: " + loanOpt.get().getPayoffAmount());
                }
            } else {
                serviceRequestResponse.setFailure("loan not found for id: " + closeLoan.getLoanId());
            }
        } catch (SQLException ex) {
            log.error("receivedCloseLoanMessage exception {}", ex.getMessage());
            serviceRequestResponse.setError("receivedCloseLoanMessage exception " + ex.getMessage());
        } finally {
            mqProducers.serviceRequestServiceRequest(serviceRequestResponse);
        }
    }
}
