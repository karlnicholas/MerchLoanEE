package com.github.karlnicholas.merchloan.client.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

@Component
@Slf4j
public class BusinessDateComponent {
    private final RestTemplate restTemplate;

    public BusinessDateComponent(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private ResponseEntity<Void> postBusinessDate(LocalDate businessDate) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.ALL));
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> request = new HttpEntity<>(businessDate.format(DateTimeFormatter.ISO_DATE), headers);
        return restTemplate.exchange("http://localhost:8100/api/businessdate", HttpMethod.POST, request, Void.class);
    }

    private ResponseEntity<Boolean> checkStillProcessing() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.ALL));
        return restTemplate.exchange("http://localhost:8090/api/query/checkrequests", HttpMethod.GET, null, Boolean.class);
    }

    public boolean updateBusinessDate(LocalDate localDate) {
        int requestCount = 0;
        boolean loop = true;
        do {
            try {
                ResponseEntity<Boolean> stillProcessingResp = checkStillProcessing();
                if ( stillProcessingResp.getStatusCode().isError() || stillProcessingResp.getBody().booleanValue() == true) {
                    return false;
                }
                loop = false;
            } catch (Exception ex) {
                if (requestCount >= 3) {
                    log.warn("BUSINESS DATE UPDATE EXCEPTION: {}", ex.getMessage());
                    loop = false;
                }
            }
            requestCount++;
            if ( requestCount > 3 ) {
                loop = false;
            }
        } while (loop);
        if ( requestCount >= 3) {
            return false;
        }
        // Open Account
        do {
            try {
                ResponseEntity<Void> requestResponse = postBusinessDate(localDate);
                loop = requestResponse.getStatusCode().isError();
            } catch (Exception ex) {
                if (requestCount >= 3) {
                    log.warn("BUSINESS DATE UPDATE EXCEPTION: {}", ex.getMessage());
                    loop = false;
                }
            }
            requestCount++;
            if ( requestCount > 3 ) {
                loop = false;
            }
        } while (loop);
        return (requestCount < 3);
    }

}
