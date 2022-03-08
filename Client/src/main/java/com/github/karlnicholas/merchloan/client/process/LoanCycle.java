package com.github.karlnicholas.merchloan.client.process;

import com.github.karlnicholas.merchloan.client.component.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;

import java.math.BigDecimal;
import java.time.LocalDate;


@Slf4j
public class LoanCycle implements ApplicationListener<BusinessDateEvent> {
    enum CYCLE_STATES {NEW, PAYMENT, STATEMENT, CLOSE}

    private final LoanProcessHandler newLoanHandler;
    private final LoanProcessHandler paymentLoanHandler;
    private final LoanProcessHandler loanStatementHandler;
    private final LoanProcessHandler closeLoanHandler;
    private LoanProcessHandler currentLoanHandler;
    private LocalDate eventDate;
    private CYCLE_STATES cycleState;
    private LoanData loanData;

    public LoanCycle(CreditComponent creditComponent, AccountComponent accountComponent, LoanComponent loanComponent, CloseComponent closeComponent, LoanStateComponent loanStateComponent, String customer) {
        eventDate = LocalDate.now();
        newLoanHandler = new NewLoanHandler(accountComponent, loanComponent, loanStateComponent);
        paymentLoanHandler = new LoanPaymentHandler(creditComponent, loanStateComponent);
        loanStatementHandler = new LoanStatementHandler(loanStateComponent);
        closeLoanHandler = new CloseLoanHandler(closeComponent, loanStateComponent);
        currentLoanHandler = newLoanHandler;
        cycleState = CYCLE_STATES.NEW;
        loanData = LoanData.builder()
                .customer(customer)
                .fundingAmount(BigDecimal.valueOf(10000.00))
                .build();
    }

    @Override
    public void onApplicationEvent(BusinessDateEvent event) {
        if (event.getMessage().isEqual(eventDate)) {
            log.debug("BUSINESS DATE EVENT MATCHED: {} {} {}", eventDate, cycleState, loanData);
            boolean success = currentLoanHandler.progressState(loanData);
            if (success) {
                if (cycleState == CYCLE_STATES.NEW) {
                    changeToPayment();
                } else if (cycleState == CYCLE_STATES.PAYMENT) {
                    changeToStatement();
                } else if (cycleState == CYCLE_STATES.STATEMENT) {
                    changeToPaymentOrClosed();
                } else if (cycleState == CYCLE_STATES.CLOSE) {
                    log.debug("Loan Closed: {}", event.getMessage());
                }
            } else {
                log.error("Loan Cycle failed: {}", event.getMessage());
            }
        }
    }

    private void changeToPaymentOrClosed() {
        if ( loanData.getLastStatementDate().plusMonths(1).isEqual(loanData.getLoanState().getStartDate().plusYears(1))) {
            currentLoanHandler = closeLoanHandler;
            cycleState = CYCLE_STATES.CLOSE;
        } else {
            currentLoanHandler = paymentLoanHandler;
            cycleState = CYCLE_STATES.PAYMENT;
        }
        eventDate = loanData.getLastStatementDate().plusDays(20);
    }

    private void changeToStatement() {
        if (loanData.getLoanState().getLastStatementDate() == null) {
            eventDate = loanData.getLoanState().getStartDate().plusMonths(1).plusDays(1);
        } else {
            eventDate = loanData.getLoanState().getLastStatementDate().plusMonths(1).plusDays(1);
        }
        loanData.setLastStatementDate(eventDate.minusDays(1));
        cycleState = CYCLE_STATES.STATEMENT;
        currentLoanHandler = loanStatementHandler;
    }

    private void changeToPayment() {
        currentLoanHandler = paymentLoanHandler;
        eventDate = loanData.getLoanState().getStartDate().plusDays(20);
        cycleState = CYCLE_STATES.PAYMENT;
    }
}
