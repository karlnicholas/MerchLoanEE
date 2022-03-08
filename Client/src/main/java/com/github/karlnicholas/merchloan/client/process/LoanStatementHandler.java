package com.github.karlnicholas.merchloan.client.process;

import com.github.karlnicholas.merchloan.client.component.LoanStateComponent;
import com.github.karlnicholas.merchloan.dto.LoanDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class LoanStatementHandler implements LoanProcessHandler {
    private final LoanStateComponent loanStateComponent;

    public LoanStatementHandler(LoanStateComponent loanStateComponent) {
        this.loanStateComponent = loanStateComponent;
    }

    @Override
    public boolean progressState(LoanData loanData) {
        // Check request status
        int requestCount = 0;
        boolean loop = true;
        int waitTime = 2000;
        do {
            sleep(waitTime);
            try {
                Optional<LoanDto> loanState = loanStateComponent.checkLoanStatus(loanData.getLoanId());
                if ( loanState.isPresent() ) {
                    LoanDto loanDto = loanState.get();
                    if ( loanDto.getLastStatementDate().isEqual(loanData.getLastStatementDate())) {
                        loanData.setLoanState(loanDto);
                        return true;
                    }
                }
            } catch ( Exception ex) {
                if ( requestCount >= 3 ) {
                    log.warn("Request Status exception: {}", ex.getMessage());
                    loop = false;
                }
            }
            requestCount++;
            waitTime *= 3;
        } while (loop);
        return false;
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
