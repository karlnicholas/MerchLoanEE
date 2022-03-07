package com.github.karlnicholas.merchloan.client.process;

import com.github.karlnicholas.merchloan.client.component.CloseComponent;
import com.github.karlnicholas.merchloan.dto.LoanDto;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

public class LoanStateCloseHandler implements LoanStateHandler {
    private final CloseComponent closeComponent;
    private final RestTemplate restTemplate;

    public LoanStateCloseHandler(CloseComponent closeComponent, RestTemplate restTemplate) {
        this.closeComponent = closeComponent;
        this.restTemplate = restTemplate;
    }

    @Override
    public Optional<LoanDto> progressState(Object... args) {
        return Optional.empty();
    }
}
