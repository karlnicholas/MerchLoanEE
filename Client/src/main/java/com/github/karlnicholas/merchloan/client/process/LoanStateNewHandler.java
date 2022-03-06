package com.github.karlnicholas.merchloan.client.process;

import com.github.karlnicholas.merchloan.client.component.AccountComponent;
import com.github.karlnicholas.merchloan.client.component.LoanComponent;
import com.github.karlnicholas.merchloan.client.component.LoanStatusComponent;
import com.github.karlnicholas.merchloan.dto.LoanDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class LoanStateNewHandler implements LoanStateHandler {
    private final AccountComponent accountComponent;
    private final LoanComponent loanComponent;
    private final LoanStatusComponent loanStatusComponent;

    public LoanStateNewHandler(AccountComponent accountComponent, LoanComponent loanComponent, LoanStatusComponent loanStatusComponent) {
        this.accountComponent = accountComponent;
        this.loanComponent = loanComponent;
        this.loanStatusComponent = loanStatusComponent;
    }

    @Override
    public Optional<LoanDto> progressState(Object ... args) {
        ResponseEntity<UUID> accountId = null;
        int accountCount = 1;
        do {
            try {
                accountId = accountComponent.accountRequest((String) args[0]);
            } catch ( Exception ex) {
                log.warn("CREATE ACCOUNT EXCEPTION: ", ex);
            }
        } while ( (accountId == null || (accountId != null && accountId.getStatusCode() != HttpStatus.OK)) && ++accountCount <= 3);
        if ( accountCount > 3) {
            return Optional.empty();
        }
        ResponseEntity<UUID> loanId = null;
        int loanCount = 1;
        do {
            try {
                loanId = loanComponent.fundingRequest(accountId.getBody(), (BigDecimal) args[1], (String) args[2]);
            } catch ( Exception ex) {
                log.warn("FUND LOAN EXCEPTION: ", ex);
            }
        } while ( (loanId == null || (loanId != null && loanId.getStatusCode() != HttpStatus.OK)) && ++loanCount <= 3);
        if ( loanCount > 3) {
            return Optional.empty();
        }
        ResponseEntity<LoanDto> loanDto;
        int loanDtoCount = 1;
        do {
            loanDto = loanStatusComponent.loanStatus(loanId.getBody());
        } while ( (loanDto == null || (loanDto != null && loanDto.getStatusCode() != HttpStatus.OK)) && ++loanDtoCount <= 3);
        if ( loanDtoCount > 3) {
            return Optional.empty();
        }
        return Optional.ofNullable(loanDto.getBody());
    }
}
