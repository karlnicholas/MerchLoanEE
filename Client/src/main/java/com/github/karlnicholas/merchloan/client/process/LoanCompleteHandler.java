package com.github.karlnicholas.merchloan.client.process;

import com.github.karlnicholas.merchloan.client.component.CloseComponent;
import com.github.karlnicholas.merchloan.client.component.LoanStateComponent;
import com.github.karlnicholas.merchloan.client.component.RequestStatusComponent;
import com.github.karlnicholas.merchloan.dto.LoanDto;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.UUID;

@Slf4j
public class LoanCompleteHandler implements LoanProcessHandler {

    @Override
    public boolean progressState(LoanData loanData) {
        log.info("COMPLETE: {}", loanData);
        return true;
    }
}
