package com.github.karlnicholas.merchloan.client.process;

import com.github.karlnicholas.merchloan.client.component.CreditComponent;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class LoanPaymentHandler implements LoanProcessHandler {
    private final CreditComponent creditComponent;

    public LoanPaymentHandler(CreditComponent creditComponent) {
        this.creditComponent = creditComponent;
    }

    @Override
    public boolean progressState(LoanData loanData) {
        Optional<UUID> creditId = creditComponent.makePayment(loanData.getLoanId(), loanData.getLoanState().getCurrentPayment(), LoanData.PAYMENT_DESCRIPTION);
        if ( creditId == null || creditId.isEmpty()) {
            return false;
        }
        loanData.setLastPaymentRequestId(creditId.get());
        return true;
    }
}
