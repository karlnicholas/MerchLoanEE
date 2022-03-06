package com.github.karlnicholas.merchloan.client.component;

import com.github.karlnicholas.merchloan.apimessage.message.FundingRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

@Component
public class LoanComponent {
    private final RestTemplate restTemplate;

    public LoanComponent(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<UUID> fundingRequest(UUID accountId, BigDecimal amount, String description) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.TEXT_PLAIN));
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<FundingRequest> request = new HttpEntity<>(new FundingRequest(accountId, amount, description), headers);
        return restTemplate.exchange("http://localhost:8080/api/v1/service/fundingRequest", HttpMethod.POST, request, UUID.class);
    }
}
