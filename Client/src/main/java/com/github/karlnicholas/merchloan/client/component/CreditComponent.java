package com.github.karlnicholas.merchloan.client.component;

import com.github.karlnicholas.merchloan.apimessage.message.CreditRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

@Component
public class CreditComponent {
    private final RestTemplate restTemplate;

    public CreditComponent(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<UUID> creditRequest(UUID loanId, BigDecimal amount, String description) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.ALL));
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreditRequest> request = new HttpEntity<>(new CreditRequest(loanId, amount, description), headers);
        return restTemplate.exchange("http://localhost:8080/api/v1/service/creditRequest", HttpMethod.POST, null, UUID.class);
    }
}
