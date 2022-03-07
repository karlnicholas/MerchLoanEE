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
    private final RequestStatusComponent requestStatusComponent;

    public AccountComponent(RequestStatusComponent requestStatusComponent, RestTemplate restTemplate) {
        this.requestStatusComponent = requestStatusComponent;
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<UUID> accountRequest(String customer) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.TEXT_PLAIN));
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AccountRequest> request = new HttpEntity<>(new AccountRequest(customer), headers);
        return restTemplate.exchange("http://localhost:8080/api/v1/service/accountRequest", HttpMethod.POST, request, UUID.class);
    }

    public Optional<UUID> createAccount(String customer) {
        // Open Account
        ResponseEntity<UUID> accountId = null;
        int accountCount = 1;
        do {
            try {
                accountId = accountRequest(customer);
            } catch (Exception ex) {
                if (accountCount == 3)
                    log.warn("CREATE ACCOUNT EXCEPTION: ", ex);
            }
        } while ((accountId != null && accountId.getStatusCode() != HttpStatus.OK) && ++accountCount <= 3);
        if (accountCount > 3 || accountId == null) {
            return Optional.empty();
        }
        if ( requestStatusComponent.checkRequestStatus(accountId.getBody()).isEmpty() ) {
            return Optional.empty();
        }
        return Optional.of(accountId.getBody());
    }
}
