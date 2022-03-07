package com.github.karlnicholas.merchloan.client.process;

import com.github.karlnicholas.merchloan.client.component.AccountComponent;
import com.github.karlnicholas.merchloan.client.component.LoanComponent;
import com.github.karlnicholas.merchloan.client.component.LoanStateComponent;
import com.github.karlnicholas.merchloan.dto.LoanDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class NewLoanProcessHandler implements LoanProcessHandler {
    private final AccountComponent accountComponent;
    private final LoanComponent loanComponent;
    private final LoanStateComponent loanStateComponent;

    public NewLoanProcessHandler(AccountComponent accountComponent, LoanComponent loanComponent, LoanStateComponent loanStateComponent) {
        this.accountComponent = accountComponent;
        this.loanComponent = loanComponent;
        this.loanStateComponent = loanStateComponent;
    }

    @Override
    public boolean progressState(LoanData loanData) {
        // Open Account
        Optional<UUID> accountId = accountComponent.createAccount(loanData.getCustomer());
        if ( accountId.isEmpty()) {
            return false;
        }
        Optional<UUID> loanId = loanComponent.fundLoan(accountId.get(), loanData.getFundingAmount(), LoanData.FUNDING_DESCRIPTION);
        if ( loanId.isEmpty()) {
            return false;
        }
        loanData.setLoanId(loanId.get());
        Optional<LoanDto> loanState = loanStateComponent.checkLoanStatus(loanId.get());
        if ( loanState.isEmpty()) {
            return false;
        }
        loanData.setLoanState(loanState.get());
        return true;
    }

}
