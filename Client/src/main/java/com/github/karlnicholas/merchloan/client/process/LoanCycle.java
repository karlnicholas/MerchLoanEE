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
    private int statementIndex;

    public LoanCycle(CreditComponent creditComponent, AccountComponent accountComponent, LoanComponent loanComponent, CloseComponent closeComponent, LoanStateComponent loanStateComponent, RequestStatusComponent requestStatusComponent, LocalDate startDate, String customer) {
        eventDate = startDate;
        newLoanHandler = new NewLoanHandler(accountComponent, loanComponent, loanStateComponent, requestStatusComponent);
        paymentLoanHandler = new LoanPaymentHandler(creditComponent);
        loanStatementHandler = new LoanStatementHandler(loanStateComponent, requestStatusComponent);
        closeLoanHandler = new CloseLoanHandler(closeComponent, loanStateComponent, requestStatusComponent);
        currentLoanHandler = newLoanHandler;
        cycleState = CYCLE_STATES.NEW;
        statementIndex = 0;
        loanData = LoanData.builder()
                .customer(customer)
                .fundingAmount(BigDecimal.valueOf(10000.00))
                .build();
    }

    @Override
    public void onApplicationEvent(BusinessDateEvent event) {
        log.trace("BUSINESS DATE EVENT: {}", event.getMessage());
        if (event.getMessage().isEqual(eventDate)) {
            LocalDate saveDate = eventDate;
            CYCLE_STATES saveState = cycleState;
            boolean success = currentLoanHandler.progressState(loanData);
            if (success) {
                if (cycleState == CYCLE_STATES.NEW) {
                    changeToPaymentOrClosed();
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
//            log.debug("LOAN CYCLE: {}->{} {}->{}{}{}", saveDate, eventDate, saveState, cycleState,  System.lineSeparator(), loanData);
            log.debug("LOAN CYCLE: {}->{} {}->{} {}", saveDate, eventDate, saveState, cycleState, loanData.getLoanId().toString().substring(32));
        }
    }

    private void changeToStatement() {
        eventDate = loanData.getLoanState().getStatementDates().get(statementIndex).plusDays(4);
        loanData.setLastStatementDate(loanData.getLoanState().getStatementDates().get(statementIndex++));
        cycleState = CYCLE_STATES.STATEMENT;
        currentLoanHandler = loanStatementHandler;
    }

    private void changeToPaymentOrClosed() {
        if ( loanData.getLoanState().getStatementDates().get(statementIndex).compareTo(loanData.getLoanState().getStartDate().plusYears(1)) >= 0) {
            currentLoanHandler = closeLoanHandler;
            cycleState = CYCLE_STATES.CLOSE;
        } else {
            currentLoanHandler = paymentLoanHandler;
            cycleState = CYCLE_STATES.PAYMENT;
        }
        eventDate = loanData.getLoanState().getStatementDates().get(statementIndex).minusDays(10);
    }

}
