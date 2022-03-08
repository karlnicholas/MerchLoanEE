package com.github.karlnicholas.merchloan.client.process;

import com.github.karlnicholas.merchloan.client.component.CreditComponent;
import com.github.karlnicholas.merchloan.client.component.LoanStateComponent;
import com.github.karlnicholas.merchloan.dto.LoanDto;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class LoanPaymentHandler implements LoanProcessHandler {
    private final CreditComponent creditComponent;
    private final LoanStateComponent loanStateComponent;

    public LoanPaymentHandler(CreditComponent creditComponent, LoanStateComponent loanStateComponent) {
        this.creditComponent = creditComponent;
        this.loanStateComponent = loanStateComponent;
    }

    @Override
    public boolean progressState(LoanData loanData) {
        Optional<UUID> creditId = creditComponent.makePayment(loanData.getLoanId(), loanData.getLoanState().getCurrentPayment(), LoanData.PAYMENT_DESCRIPTION);
        if ( creditId.isEmpty()) {
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
