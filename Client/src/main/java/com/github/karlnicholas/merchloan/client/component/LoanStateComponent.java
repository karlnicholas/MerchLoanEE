package com.github.karlnicholas.merchloan.client.component;

import com.github.karlnicholas.merchloan.dto.LoanDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class LoanStateComponent {
    private final RestTemplate restTemplate;

    public LoanStateComponent(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<LoanDto> loanStatus(UUID loanId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return restTemplate.getForEntity("http://localhost:8090/api/query/loan/{loanId}", LoanDto.class, loanId);
    }

    public Optional<LoanDto> checkLoanStatus(UUID loanId) {
        // Return Loan State
        ResponseEntity<LoanDto> loanDto = null;
        int loanDtoCount = 1;
        do {
            try {
                loanDto = loanStatus(loanId);
            } catch (Exception ex) {
                if (loanDtoCount == 3)
                    log.warn("LOAN STATE EXCEPTION: ", ex);
            }
        } while ((loanDto != null && loanDto.getStatusCode() != HttpStatus.OK) && ++loanDtoCount <= 3);
        if (loanDtoCount > 3 || loanDto == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(loanDto.getBody());
    }
}
