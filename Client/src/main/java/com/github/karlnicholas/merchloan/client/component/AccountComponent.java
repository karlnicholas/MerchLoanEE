package com.github.karlnicholas.merchloan.client.component;

import com.github.karlnicholas.merchloan.apimessage.message.AccountRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

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

    public Optional<UUID> createAccount(String customer) throws ExecutionException, InterruptedException {
        // Open Account
        ResponseEntity<UUID> responseEntity = accountRequest(customer);
        return responseEntity == null ? Optional.empty() : Optional.of(responseEntity.getBody());
    }
}
