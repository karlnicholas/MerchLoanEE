package com.github.karlnicholas.merchloan.client.process;

import com.github.karlnicholas.merchloan.client.component.CloseComponent;
import com.github.karlnicholas.merchloan.client.component.LoanStateComponent;
import com.github.karlnicholas.merchloan.dto.LoanDto;

import java.util.Optional;
import java.util.UUID;

public class CloseLoanHandler implements LoanProcessHandler {
    private final CloseComponent closeComponent;
    private final LoanStateComponent loanStateComponent;

    public CloseLoanHandler(CloseComponent closeComponent, LoanStateComponent loanStateComponent) {
        this.closeComponent = closeComponent;
        this.loanStateComponent = loanStateComponent;
    }

    @Override
    public boolean progressState(LoanData loanData) {
        Optional<UUID> closeId = closeComponent.closeLoan(loanData.getLoanId(), loanData.getLoanState().getPayoffAmount(), LoanData.CLOSE_DESCRIPTION);
        if ( closeId.isEmpty()) {
            return false;
        }
        Optional<LoanDto> loanState = loanStateComponent.checkLoanStatus(loanData.getLoanId());
        if ( loanState.isEmpty()) {
            return false;
        }
        loanData.setLoanState(loanState.get());
        return true;
    }
}
