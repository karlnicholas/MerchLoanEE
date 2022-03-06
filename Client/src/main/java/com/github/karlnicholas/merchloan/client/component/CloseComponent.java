package com.github.karlnicholas.merchloan.client.component;

import com.github.karlnicholas.merchloan.apimessage.message.CloseRequest;
import com.github.karlnicholas.merchloan.apimessage.message.CreditRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

@Component
public class CloseComponent {
    private final RestTemplate restTemplate;

    public CloseComponent(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<UUID> closeRequest(UUID loanId, BigDecimal amount, String description) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.ALL));
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CloseRequest> request = new HttpEntity<>(new CloseRequest(loanId, amount, description));
        return restTemplate.exchange("http://localhost:8080/api/v1/service/closeRequest", HttpMethod.POST, null, UUID.class);
    }
}
