package com.github.karlnicholas.merchloan.client.component;

import com.github.karlnicholas.merchloan.dto.LoanDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    private ResponseEntity<LoanDto> loanStatus(UUID loanId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return restTemplate.exchange("http://localhost:8090/api/query/loan/{loanId}", HttpMethod.GET, null, LoanDto.class, loanId);
    }

    public Optional<LoanDto> checkLoanStatus(UUID loanId) {
        // Return Loan State
        boolean loop = true;
        int requestCount = 0;
        do {
            try {
                ResponseEntity<LoanDto> loanDto = loanStatus(loanId);
                loop = loanDto.getStatusCode().isError();
                if (!loop) {
                    return Optional.of(loanDto.getBody());
                }
            } catch (Exception ex) {
                if (requestCount == 3) {
                    log.warn("LOAN STATE EXCEPTION: {}", ex.getMessage());
                    loop = false;
                }
            }
            requestCount++;
            if (requestCount > 3) {
                loop = false;
            }
        } while (loop);
        return Optional.empty();
    }
}
