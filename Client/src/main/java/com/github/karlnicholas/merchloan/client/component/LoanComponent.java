package com.github.karlnicholas.merchloan.client.component;

import com.github.karlnicholas.merchloan.apimessage.message.FundingRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class LoanComponent {
    private final RestTemplate restTemplate;

    public LoanComponent(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private ResponseEntity<UUID> fundingRequest(UUID accountId, BigDecimal amount, String description) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.TEXT_PLAIN));
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<FundingRequest> request = new HttpEntity<>(new FundingRequest(accountId, amount, description), headers);
        return restTemplate.exchange("http://localhost:8080/api/v1/service/fundingRequest", HttpMethod.POST, request, UUID.class);
    }

    public Optional<UUID> fundLoan(UUID accountId, BigDecimal amount, String description) {
        // Fund Loan
        int requestCount = 0;
        boolean loop = true;
        do {
            try {
                ResponseEntity<UUID> loanId = fundingRequest(accountId, amount, description);
                loop = loanId.getStatusCode().isError();
                if (!loop) {
                    return Optional.of(loanId.getBody());
                }
            } catch (Exception ex) {
                if (requestCount >= 3) {
                    log.warn("FUND LOAN EXCEPTION: {}", ex.getMessage());
                    loop = false;
                }
            }
            requestCount++;
        } while (loop);
        return Optional.empty();
    }
}
