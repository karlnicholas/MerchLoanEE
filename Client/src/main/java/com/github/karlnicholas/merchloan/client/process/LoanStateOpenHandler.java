package com.github.karlnicholas.merchloan.client.process;

import com.github.karlnicholas.merchloan.client.component.LoanStatusComponent;
import com.github.karlnicholas.merchloan.dto.LoanDto;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

public class LoanStateOpenHandler implements LoanStateHandler {
    private final LoanStatusComponent loanStatusComponent;
    private final RestTemplate restTemplate;

    public LoanStateOpenHandler(LoanStatusComponent loanStatusComponent, RestTemplate restTemplate) {
        this.loanStatusComponent = loanStatusComponent;
        this.restTemplate = restTemplate;
    }

    @Override
    public Optional<LoanDto> progressState(Object... args) {
        return Optional.empty();
    }
}
