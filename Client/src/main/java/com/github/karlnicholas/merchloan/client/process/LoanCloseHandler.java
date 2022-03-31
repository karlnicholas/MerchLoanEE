package com.github.karlnicholas.merchloan.client.process;

import com.github.karlnicholas.merchloan.client.component.CloseComponent;
import com.github.karlnicholas.merchloan.client.component.LoanStateComponent;
import com.github.karlnicholas.merchloan.client.component.RequestStatusComponent;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.UUID;

@Slf4j
public class LoanCloseHandler implements LoanProcessHandler {
    private final CloseComponent closeComponent;
    private final LoanStateComponent loanStateComponent;
    private final RequestStatusComponent requestStatusComponent;

    public LoanCloseHandler(CloseComponent closeComponent, LoanStateComponent loanStateComponent, RequestStatusComponent requestStatusComponent) {
        this.closeComponent = closeComponent;
        this.loanStateComponent = loanStateComponent;
        this.requestStatusComponent = requestStatusComponent;
    }

    @Override
    public boolean progressState(LoanData loanData) {
        Optional<UUID> closeId = closeComponent.closeLoan(loanData.getLoanId(), loanData.getLoanState().getPayoffAmount(), LoanData.CLOSE_DESCRIPTION);
        if ( closeId == null || closeId.isEmpty()) {
            return false;
        }
        loanData.setLastPaymentRequestId(closeId.get());
        return true;
    }
}
