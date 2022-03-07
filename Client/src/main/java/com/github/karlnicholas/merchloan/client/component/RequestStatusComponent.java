package com.github.karlnicholas.merchloan.client.component;

import com.github.karlnicholas.merchloan.dto.RequestStatusDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.UUID;

@Component
public class RequestStatusComponent {
    private final RestTemplate restTemplate;

    public RequestStatusComponent(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<RequestStatusDto> requestStatus(UUID id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return restTemplate.getForEntity("http://localhost:8090/api/query/request/{id}", RequestStatusDto.class, id);
    }
}
