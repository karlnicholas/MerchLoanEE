package com.github.karlnicholas.merchloan.client.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.karlnicholas.merchloan.apimessage.message.CloseRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class CloseComponent {
    private final ObjectMapper objectMapper;

    public CloseComponent(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private Optional<UUID> closeRequest(UUID loanId, BigDecimal amount, String description) {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            String strJson = objectMapper.writeValueAsString(new CloseRequest(loanId, amount, description));
            StringEntity strEntity = new StringEntity(strJson, ContentType.APPLICATION_JSON);
            HttpPost httpPost = new HttpPost("http://localhost:8080/api/v1/service/closeRequest");
            httpPost.setHeader("Accept", ContentType.WILDCARD.getMimeType());
//            httpPost.setHeader("Content-type", "application/json");
            httpPost.setEntity(strEntity);

            try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
                HttpEntity entity = response.getEntity();
                return Optional.of(UUID.fromString(EntityUtils.toString(entity)));
            } catch (ParseException e) {
                log.error("accountRequest", e);
            }
        } catch (IOException e) {
            log.error("accountRequest", e);
        }
        return Optional.empty();
//        HttpHeaders headers = new HttpHeaders();
//        headers.setAccept(Collections.singletonList(MediaType.ALL));
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        HttpEntity<CloseRequest> request = new HttpEntity<>(new CloseRequest(loanId, amount, description));
//        return restTemplate.exchange("http://localhost:8080/api/v1/service/closeRequest", HttpMethod.POST, request, UUID.class);
    }

    public Optional<UUID> closeLoan(UUID loanId, BigDecimal amount, String description) {
        // Open Account
        int requestCount = 0;
        boolean loop = true;
        do {
            try {
                Optional<UUID> closeId = closeRequest(loanId, amount, description);
                loop = closeId.isEmpty();
                if ( !loop ) {
                    return Optional.of(closeId.get());
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
