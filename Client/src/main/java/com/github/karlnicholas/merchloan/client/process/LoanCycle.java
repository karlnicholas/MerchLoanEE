package com.github.karlnicholas.merchloan.client.process;

import com.github.karlnicholas.merchloan.client.component.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;


@Slf4j
public class LoanCycle implements ApplicationListener<BusinessDateEvent> {
    enum LOAN_STATES {NEW, OPEN, CLOSED}

    private final CreditComponent creditComponent;
    private final LoanStateComponent loanStateComponent;
    private final CloseComponent closeComponent;
    private LoanProcessHandler loanProcessHandler;
    private LocalDate eventDate;
    private LOAN_STATES loanState;
    private LoanData loanData;

    public LoanCycle(CreditComponent creditComponent, AccountComponent accountComponent, LoanComponent loanComponent, CloseComponent closeComponent, LoanStateComponent loanStateComponent, String customer) {
        this.creditComponent = creditComponent;
        this.closeComponent = closeComponent;
        this.loanStateComponent = loanStateComponent;
        eventDate = LocalDate.now();
        loanProcessHandler = new NewLoanProcessHandler(accountComponent, loanComponent, loanStateComponent);
        loanState = LOAN_STATES.NEW;
        loanData = LoanData.builder()
                .customer(customer)
                .fundingAmount(BigDecimal.valueOf(10000.00))
                .build();
    }

    @Override
    public void onApplicationEvent(BusinessDateEvent event) {

        log.info("Received spring custom event - {}", event.getMessage());
        if (event.getMessage().isEqual(eventDate)) {
            boolean success = loanProcessHandler.progressState(loanData);
            if (success) {
                if (loanState == LOAN_STATES.NEW) {
                    loanProcessHandler = new OpenLoanProcessHandler(creditComponent, loanStateComponent);
                    eventDate = loanData.getLoanState().getStartDate().plusMonths(1);
                    loanState = LOAN_STATES.OPEN;
                } else if ( loanState == LOAN_STATES.OPEN) {
                    eventDate = loanData.getLoanState().getLastStatementDate().plusMonths(1);
                    if (eventDate.isEqual(loanData.getLoanState().getStartDate().plusYears(1))) {
                        loanProcessHandler = new CloseLoanProcessHandler(closeComponent, loanStateComponent);
                        loanState = LOAN_STATES.CLOSED;
                    }
                }
            }
        }
    }
}
