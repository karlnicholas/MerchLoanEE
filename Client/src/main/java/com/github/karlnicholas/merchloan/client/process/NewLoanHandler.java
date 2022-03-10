package com.github.karlnicholas.merchloan.client.process;

import com.github.karlnicholas.merchloan.client.component.AccountComponent;
import com.github.karlnicholas.merchloan.client.component.LoanComponent;
import com.github.karlnicholas.merchloan.client.component.LoanStateComponent;
import com.github.karlnicholas.merchloan.client.component.RequestStatusComponent;
import com.github.karlnicholas.merchloan.dto.LoanDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class NewLoanHandler implements LoanProcessHandler {
    private final AccountComponent accountComponent;
    private final LoanComponent loanComponent;
    private final LoanStateComponent loanStateComponent;
    private final RequestStatusComponent requestStatusComponent;

    public NewLoanHandler(AccountComponent accountComponent, LoanComponent loanComponent, LoanStateComponent loanStateComponent, RequestStatusComponent requestStatusComponent) {
        this.accountComponent = accountComponent;
        this.loanComponent = loanComponent;
        this.loanStateComponent = loanStateComponent;
        this.requestStatusComponent = requestStatusComponent;
    }

    @Override
    public boolean progressState(LoanData loanData) {
        // Open Account
        Optional<UUID> accountId = accountComponent.createAccount(loanData.getCustomer());
        if ( accountId.isEmpty()) {
            return false;
        }
        sleep(300);
        Optional<UUID> requestId = requestStatusComponent.checkRequestStatus(accountId.get());
        if ( requestId.isEmpty()) {
            return false;
        }
        Optional<UUID> loanId = loanComponent.fundLoan(accountId.get(), loanData.getFundingAmount(), LoanData.FUNDING_DESCRIPTION);
        if ( loanId.isEmpty()) {
            return false;
        }
        loanData.setLoanId(loanId.get());
        sleep(300);
        requestId = requestStatusComponent.checkRequestStatus(loanId.get());
        if ( requestId.isEmpty()) {
            return false;
        }
        Optional<LoanDto> loanState = loanStateComponent.checkLoanStatus(loanId.get());
        if ( loanState.isEmpty()) {
            return false;
        }
        loanData.setLoanState(loanState.get());
        return true;
    }

    private void sleep(int waitTime) {
        try {
            Thread.sleep(waitTime);
        } catch ( InterruptedException ex) {
            log.error("Sleep while check status interrupted: {}", ex.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
