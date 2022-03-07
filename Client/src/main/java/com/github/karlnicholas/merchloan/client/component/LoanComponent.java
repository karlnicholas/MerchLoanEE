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
    private final RequestStatusComponent requestStatusComponent;

    public LoanComponent(RestTemplate restTemplate, RequestStatusComponent requestStatusComponent) {
        this.restTemplate = restTemplate;
        this.requestStatusComponent = requestStatusComponent;
    }

    public ResponseEntity<UUID> fundingRequest(UUID accountId, BigDecimal amount, String description) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.TEXT_PLAIN));
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<FundingRequest> request = new HttpEntity<>(new FundingRequest(accountId, amount, description), headers);
        return restTemplate.exchange("http://localhost:8080/api/v1/service/fundingRequest", HttpMethod.POST, request, UUID.class);
    }

    public Optional<UUID> fundLoan(UUID accountId, BigDecimal amount, String description) {
        // Fund Loan
        ResponseEntity<UUID> loanId = null;
        int loanCount = 1;
        do {
            try {
                loanId = fundingRequest(accountId, amount, description);
            } catch (Exception ex) {
                if (loanCount == 3)
                    log.warn("FUND LOAN EXCEPTION: ", ex);
            }
        } while ((loanId != null && loanId.getStatusCode() != HttpStatus.OK) && ++loanCount <= 3);
        if (loanCount > 3 || loanId == null) {
            return Optional.empty();
        }
        if (requestStatusComponent.checkRequestStatus(loanId.getBody()).isEmpty() ) {
            return Optional.empty();
        }
        return Optional.of(loanId.getBody());
    }
}
