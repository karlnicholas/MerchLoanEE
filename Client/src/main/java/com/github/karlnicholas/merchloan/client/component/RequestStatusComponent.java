package com.github.karlnicholas.merchloan.client.component;

import com.github.karlnicholas.merchloan.dto.RequestStatusDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
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

    private ResponseEntity<RequestStatusDto> requestStatus(UUID id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return restTemplate.getForEntity("http://localhost:8090/api/query/request/{id}", RequestStatusDto.class, id);
    }

    public Optional<UUID> checkRequestStatus(UUID id) {
        // Check request status
        int requestCount = 0;
        boolean loop = true;
        int waitTime = 300;
        do {
            try {
                ResponseEntity<RequestStatusDto> requestStatusDto = requestStatus(id);
                loop = requestStatusDto.getStatusCode().isError();
                if (!loop) {
                    RequestStatusDto statusDto = requestStatusDto.getBody();
                    if (statusDto != null && statusDto.getStatus().compareToIgnoreCase("SUCCESS") == 0) {
                        return Optional.of(id);
                    } else {
                        // try again
                        loop = true;
                    }
                }
            } catch (Exception ex) {
                if (requestCount >= 3) {
                    log.warn("Request Status exception: {}", ex.getMessage());
                    loop = false;
                }
            }
            if (loop) {
                sleep(waitTime);
            }
            requestCount++;
            waitTime *= 3;
        } while (loop);
        return Optional.empty();
    }

    private void sleep(int waitTime) {
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException ex) {
            log.error("Sleep while check status interrupted: {}", ex.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
