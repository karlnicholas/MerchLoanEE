package com.github.karlnicholas.merchloan.client.component;

import com.github.karlnicholas.merchloan.apimessage.message.CloseRequest;
import com.github.karlnicholas.merchloan.dto.LoanDto;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

@Component
public class LoanStatusComponent {
    private final RestTemplate restTemplate;

    public LoanStatusComponent(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<LoanDto> loanStatus(UUID loanId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return restTemplate.getForEntity("http://localhost:8090/api/query/loan/{loanId}", LoanDto.class, loanId);
    }
}
