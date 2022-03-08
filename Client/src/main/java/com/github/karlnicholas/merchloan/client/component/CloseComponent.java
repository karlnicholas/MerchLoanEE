package com.github.karlnicholas.merchloan.client.component;

import com.github.karlnicholas.merchloan.apimessage.message.CloseRequest;
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
public class CloseComponent {
    private final RestTemplate restTemplate;
    private final RequestStatusComponent requestStatusComponent;

    public CloseComponent(RestTemplate restTemplate, RequestStatusComponent requestStatusComponent) {
        this.restTemplate = restTemplate;
        this.requestStatusComponent = requestStatusComponent;
    }

    private ResponseEntity<UUID> closeRequest(UUID loanId, BigDecimal amount, String description) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.ALL));
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CloseRequest> request = new HttpEntity<>(new CloseRequest(loanId, amount, description));
        return restTemplate.exchange("http://localhost:8080/api/v1/service/closeRequest", HttpMethod.POST, request, UUID.class);
    }

    public Optional<UUID> closeLoan(UUID loanId, BigDecimal amount, String description) {
        // Open Account
        int requestCount = 0;
        boolean loop = true;
        do {
            try {
                ResponseEntity<UUID> closeId = closeRequest(loanId, amount, description);
                loop = closeId.getStatusCode().isError();
                if ( !loop ) {
                    return requestStatusComponent.checkRequestStatus(closeId.getBody());
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
