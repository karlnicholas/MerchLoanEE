package com.github.karlnicholas.merchloan.client.process;

import com.github.karlnicholas.merchloan.client.component.CloseComponent;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.UUID;

@Slf4j
public class LoanCloseHandler implements LoanProcessHandler {
    private final CloseComponent closeComponent;

    public LoanCloseHandler(CloseComponent closeComponent ) {
        this.closeComponent = closeComponent;
    }

    @Override
    public boolean progressState(LoanData loanData) {
        Optional<UUID> closeId = closeComponent.closeLoan(loanData.getLoanId(), loanData.getLoanState().getPayoffAmount(), LoanData.CLOSE_DESCRIPTION);
        if ( closeId.isEmpty()) {
            return false;
        }
        loanData.setLastPaymentRequestId(closeId.get());
        return true;
    }
}
