package com.github.karlnicholas.merchloan.client.component;

import com.github.karlnicholas.merchloan.apimessage.message.CreditRequest;
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
public class CreditComponent {
    private final RestTemplate restTemplate;
    private final RequestStatusComponent requestStatusComponent;

    public CreditComponent(RestTemplate restTemplate, RequestStatusComponent requestStatusComponent) {
        this.restTemplate = restTemplate;
        this.requestStatusComponent = requestStatusComponent;
    }

    private ResponseEntity<UUID> creditRequest(UUID loanId, BigDecimal amount, String description) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.ALL));
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreditRequest> request = new HttpEntity<>(new CreditRequest(loanId, amount, description), headers);
        return restTemplate.exchange("http://localhost:8080/api/v1/service/creditRequest", HttpMethod.POST, request, UUID.class);
    }

    public Optional<UUID> makePayment(UUID loanId, BigDecimal amount, String description) {
        // Open Account
        int requestCount = 0;
        boolean loop = true;
        do {
            try {
                ResponseEntity<UUID> creditId = creditRequest(loanId, amount, description);
                loop = creditId.getStatusCode().isError();
                if (!loop) {
                    return requestStatusComponent.checkRequestStatus(creditId.getBody());
                }
            } catch (Exception ex) {
                if (requestCount >= 3) {
                    log.warn("CREATE ACCOUNT EXCEPTION: {}", ex.getMessage());
                    loop = false;
                }
            }
            requestCount++;
        } while (loop);
        return Optional.empty();
    }
}
