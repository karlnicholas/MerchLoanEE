package com.github.karlnicholas.merchloan.client.component;

import com.github.karlnicholas.merchloan.apimessage.message.AccountRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class AccountComponent {
    private final RestTemplate restTemplate;

    public AccountComponent(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private ResponseEntity<UUID> accountRequest(String customer) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.TEXT_PLAIN));
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AccountRequest> request = new HttpEntity<>(new AccountRequest(customer), headers);
        return restTemplate.exchange("http://localhost:8080/api/v1/service/accountRequest", HttpMethod.POST, request, UUID.class);
    }

    public Optional<UUID> createAccount(String customer) {
        // Open Account
        int requestCount = 0;
        boolean loop = true;
        do {
            try {
                ResponseEntity<UUID> accountId = accountRequest(customer);
                loop = accountId.getStatusCode().isError();
                if ( !loop ) {
                    return Optional.of(accountId.getBody());
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
