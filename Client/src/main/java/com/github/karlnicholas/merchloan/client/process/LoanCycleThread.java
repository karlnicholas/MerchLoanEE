package com.github.karlnicholas.merchloan.client.process;

import com.github.karlnicholas.merchloan.client.component.*;
import com.github.karlnicholas.merchloan.dto.LoanDto;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;


@Slf4j
public class LoanCycleThread extends Thread {
    enum CYCLE_STATES {NEW, PAYMENT, STATEMENT}

    private final BusinessDateMonitor businessDateMonitor;
    private final LoanProcessHandler newLoanHandler;
    private final LoanProcessHandler paymentLoanHandler;
    private final LoanProcessHandler loanStatementHandler;
    private final LoanStateComponent loanStateComponent;
    private LoanProcessHandler currentLoanHandler;
    private LocalDate currentDate;
    private LocalDate cycleDate;
    private CYCLE_STATES cycleState;
    private LoanData loanData;
    private int statementIndex;

    public LoanCycleThread(CreditComponent creditComponent, AccountComponent accountComponent, LoanComponent loanComponent, LoanStateComponent loanStateComponent, RequestStatusComponent requestStatusComponent, BusinessDateMonitor businessDateMonitor, LocalDate startDate, String customer) {
        this.businessDateMonitor = businessDateMonitor;
        cycleDate = startDate;
        this.loanStateComponent = loanStateComponent;
        newLoanHandler = new NewLoanHandler(accountComponent, loanComponent, loanStateComponent, requestStatusComponent);
        paymentLoanHandler = new LoanPaymentHandler(creditComponent);
        loanStatementHandler = new LoanStatementHandler(loanStateComponent, requestStatusComponent);
        currentLoanHandler = newLoanHandler;
        cycleState = CYCLE_STATES.NEW;
        statementIndex = 0;
        loanData = LoanData.builder()
                .customer(customer)
                .fundingAmount(BigDecimal.valueOf(10000.00))
                .build();
    }

    public void showStatement() {
        Optional<LoanDto> loanDto = loanStateComponent.checkLoanStatus(loanData.getLoanId());
        if ( loanDto.isPresent())
            log.info("{}", loanDto.get());
    }

    public void run() {
        try {
            do {
                synchronized (businessDateMonitor) {
                    if (currentDate == null || currentDate.isEqual(businessDateMonitor.retrieveDate())) {
                        businessDateMonitor.wait();
                    }
                }
                currentDate = businessDateMonitor.retrieveDate();
//                log.info(Thread.currentThread().getName() + ": " + currentDate);
                if (currentDate != null && cycleDate.compareTo(currentDate) <= 0 ) {
                    LocalDate saveDate = cycleDate;
                    CYCLE_STATES saveState = cycleState;
                    boolean success = currentLoanHandler.progressState(loanData);
                    if (success) {
                        if (cycleState == CYCLE_STATES.NEW) {
                            changeToPaymentOrComplete();
                        } else if (cycleState == CYCLE_STATES.PAYMENT) {
                            changeToStatement();
                        } else if (cycleState == CYCLE_STATES.STATEMENT) {
                            changeToPaymentOrComplete();
                        }
                    } else {
                        log.error("Loan Cycle failed: {}", currentDate);
                        cycleDate = null;
                    }
                    log.debug("{} {}: {}=>{} {}=>{} {} LAST: {} STATE: {} LAST_STATEMENT_DATE: {}", success, Thread.currentThread().getName(), saveDate, cycleDate, saveState, cycleState, loanData.getLoanId(), loanData.getLastPaymentRequestId(), loanData.getLoanState().getLoanState(), loanData.getLastStatementDate());
                }
            } while (currentDate != null &&  cycleDate != null);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    private void changeToStatement() {
        cycleDate = loanData.getLoanState().getStatementDates().get(statementIndex).plusDays(4);
        loanData.setLastStatementDate(loanData.getLoanState().getStatementDates().get(statementIndex++));
        cycleState = CYCLE_STATES.STATEMENT;
        currentLoanHandler = loanStatementHandler;
    }

    private void changeToPaymentOrComplete() {
//        if ( statementIndex >= loanData.getLoanState().getStatementDates().size()) {
//            currentLoanHandler = completeHandler;
//            cycleState = CYCLE_STATES.COMPLETE;
//        } else if ( loanData.getLoanState().getStatementDates().get(statementIndex).compareTo(loanData.getLoanState().getStartDate().plusYears(1)) >= 0) {
//            currentLoanHandler = closeLoanHandler;
//            cycleState = CYCLE_STATES.CLOSE;
//        } else {
//            currentLoanHandler = paymentLoanHandler;
//            cycleState = CYCLE_STATES.PAYMENT;
//        }
        if ( loanData.getLoanState().getLoanState().equalsIgnoreCase("CLOSED")) {
            log.debug("Loan Closed: {} {}", currentDate, loanData);
            cycleDate = null;
        } else {
            currentLoanHandler = paymentLoanHandler;
            cycleState = CYCLE_STATES.PAYMENT;
            cycleDate = loanData.getLoanState().getStatementDates().get(statementIndex).minusDays(5);
        }
    }

}
