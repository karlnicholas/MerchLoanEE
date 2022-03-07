package com.github.karlnicholas.merchloan.client.component;

import com.github.karlnicholas.merchloan.dto.RequestStatusDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
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
    public Optional<RequestStatusDto> checkRequestStatus(UUID id) {
        // Check request status
        ResponseEntity<RequestStatusDto> requestStatusDto = null;
        int requestStatusDtoCount = 1;
        boolean loop = true;
        boolean error = false;
        do {
            sleep();
            try {
                requestStatusDto = requestStatus(id);
            } catch ( Exception ex) {
                ex.printStackTrace();
            }
            if ( requestStatusDto != null ) {
                RequestStatusDto statusDto = requestStatusDto.getBody();
                if ( statusDto == null || statusDto.getStatus().compareToIgnoreCase("SUCCESS") != 0) {
                    loop = false;
                    error = true;
                }
                else if (requestStatusDto.getStatusCode() == HttpStatus.OK) {
                    loop = false;
                }
            }
            if ( ++requestStatusDtoCount > 3 ) {
                loop = false;
                error = true;
            }
        } while (loop);
        if (error) {
            return Optional.empty();
        }
        return Optional.of(requestStatusDto.getBody());
    }

    private void sleep() {
        try {
            Thread.sleep(100);
        } catch ( InterruptedException iex) {
            iex.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }
}
