package com.github.karlnicholas.merchloan.client.component;

import com.github.karlnicholas.merchloan.apimessage.message.AccountRequest;
import com.github.karlnicholas.merchloan.apimessage.message.CloseRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.UUID;

@Component
public class BusinessDateComponent {
    private final RestTemplate restTemplate;

    public BusinessDateComponent(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<Void> postBusinessDate(LocalDate businessDate) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.ALL));
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> request = new HttpEntity<>(businessDate.format(DateTimeFormatter.ISO_DATE), headers);
        return restTemplate.exchange("http://localhost:8100/api/businessdate", HttpMethod.POST, request, Void.class);
    }
}
