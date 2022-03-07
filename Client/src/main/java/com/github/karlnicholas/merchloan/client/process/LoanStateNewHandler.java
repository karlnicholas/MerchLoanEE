package com.github.karlnicholas.merchloan.client.process;

import com.github.karlnicholas.merchloan.client.component.AccountComponent;
import com.github.karlnicholas.merchloan.client.component.LoanComponent;
import com.github.karlnicholas.merchloan.client.component.LoanStatusComponent;
import com.github.karlnicholas.merchloan.client.component.RequestStatusComponent;
import com.github.karlnicholas.merchloan.dto.LoanDto;
import com.github.karlnicholas.merchloan.dto.RequestStatusDto;
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
    private final RequestStatusComponent requestStatusComponent;

    public LoanStateNewHandler(AccountComponent accountComponent, LoanComponent loanComponent, LoanStatusComponent loanStatusComponent, RequestStatusComponent requestStatusComponent) {
        this.accountComponent = accountComponent;
        this.loanComponent = loanComponent;
        this.loanStatusComponent = loanStatusComponent;
        this.requestStatusComponent = requestStatusComponent;
    }

    @Override
    public Optional<LoanDto> progressState(Object... args) {
        // Open Account
        ResponseEntity<UUID> accountId = null;
        int accountCount = 1;
        do {
            try {
                accountId = accountComponent.accountRequest((String) args[0]);
            } catch (Exception ex) {
                if (accountCount == 3)
                    log.warn("CREATE ACCOUNT EXCEPTION: ", ex);
            }
        } while ((accountId == null || (accountId != null && accountId.getStatusCode() != HttpStatus.OK)) && ++accountCount <= 3);
        if (accountCount > 3) {
            return Optional.empty();
        }
        if ( checkRequestStatus(accountId.getBody()).isEmpty() ) {
            return Optional.empty();
        }
        // Fund Loan
        ResponseEntity<UUID> loanId = null;
        int loanCount = 1;
        do {
            try {
                loanId = loanComponent.fundingRequest(accountId.getBody(), (BigDecimal) args[1], (String) args[2]);
            } catch (Exception ex) {
                if (loanCount == 3)
                    log.warn("FUND LOAN EXCEPTION: ", ex);
            }
        } while ((loanId == null || (loanId != null && loanId.getStatusCode() != HttpStatus.OK)) && ++loanCount <= 3);
        if (loanCount > 3) {
            return Optional.empty();
        }
        if ( checkRequestStatus(loanId.getBody()).isEmpty() ) {
            return Optional.empty();
        }

        // Return Loan State
        ResponseEntity<LoanDto> loanDto = null;
        int loanDtoCount = 1;
        do {
            try {
                loanDto = loanStatusComponent.loanStatus(loanId.getBody());
            } catch (Exception ex) {
                if (loanDtoCount == 3)
                    log.warn("LOAN STATE EXCEPTION: ", ex);
            }
        } while ((loanDto == null || (loanDto != null && loanDto.getStatusCode() != HttpStatus.OK)) && ++loanDtoCount <= 3);
        if (loanDtoCount > 3) {
            return Optional.empty();
        }
        return Optional.ofNullable(loanDto.getBody());
    }

    private Optional<RequestStatusDto> checkRequestStatus(UUID id) {
        // Check request status
        ResponseEntity<RequestStatusDto> requestStatusDto = null;
        int requestStatusDtoCount = 1;
        do {
            try {
                Thread.sleep(100);
                requestStatusDto = requestStatusComponent.requestStatus(id);
            } catch (Exception ex) {
                if (requestStatusDtoCount == 3)
                    log.warn("REQUEST STATUS EXCEPTION: ", ex);
            }
        } while (
                ((requestStatusDto == null || (requestStatusDto != null && requestStatusDto.getStatusCode() != HttpStatus.OK))
                        || requestStatusDto.getBody().getStatus().compareToIgnoreCase("SUCCESS") != 0)
                        && ++requestStatusDtoCount <= 3
        );
        if (requestStatusDtoCount > 3 || requestStatusDto.getBody().getStatus().compareToIgnoreCase("SUCCESS") != 0) {
            return Optional.empty();
        }
        return Optional.of(requestStatusDto.getBody());
    }
}
