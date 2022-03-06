package com.github.karlnicholas.merchloan.client.process;

import com.github.karlnicholas.merchloan.dto.LoanDto;

import java.util.Optional;

public interface LoanStateHandler {
    Optional<LoanDto> progressState(Object ... args);
}
