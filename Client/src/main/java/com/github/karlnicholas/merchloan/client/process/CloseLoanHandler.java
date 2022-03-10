package com.github.karlnicholas.merchloan.client.process;

import com.github.karlnicholas.merchloan.client.component.CloseComponent;
import com.github.karlnicholas.merchloan.client.component.LoanStateComponent;
import com.github.karlnicholas.merchloan.client.component.RequestStatusComponent;
import com.github.karlnicholas.merchloan.dto.LoanDto;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.UUID;

@Slf4j
public class CloseLoanHandler implements LoanProcessHandler {
    private final CloseComponent closeComponent;
    private final LoanStateComponent loanStateComponent;
    private final RequestStatusComponent requestStatusComponent;

    public CloseLoanHandler(CloseComponent closeComponent, LoanStateComponent loanStateComponent, RequestStatusComponent requestStatusComponent) {
        this.closeComponent = closeComponent;
        this.loanStateComponent = loanStateComponent;
        this.requestStatusComponent = requestStatusComponent;
    }

    @Override
    public boolean progressState(LoanData loanData) {
        Optional<UUID> closeId = closeComponent.closeLoan(loanData.getLoanId(), loanData.getLoanState().getPayoffAmount(), LoanData.CLOSE_DESCRIPTION);
        if ( closeId.isEmpty()) {
            return false;
        }
        sleep(300);
        Optional<UUID> requestId = requestStatusComponent.checkRequestStatus(closeId.get());
        if ( requestId.isEmpty()) {
            return false;
        }
        Optional<LoanDto> loanState = loanStateComponent.checkLoanStatus(loanData.getLoanId());
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
