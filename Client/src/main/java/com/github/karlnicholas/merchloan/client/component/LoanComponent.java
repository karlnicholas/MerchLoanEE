package com.github.karlnicholas.merchloan.client.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.karlnicholas.merchloan.apimessage.message.FundingRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class LoanComponent {
    private final ObjectMapper objectMapper;
    private final CloseableHttpClient httpclient;

    public LoanComponent(PoolingHttpClientConnectionManager connManager) {
        this.objectMapper = new ObjectMapper().findAndRegisterModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        httpclient = HttpClients.custom().setConnectionManager(connManager).build();
    }

    private Optional<UUID> fundingRequest(UUID accountId, BigDecimal amount, String description) throws JsonProcessingException {
        String strJson = objectMapper.writeValueAsString(new FundingRequest(accountId, amount, description));
        StringEntity strEntity = new StringEntity(strJson, ContentType.APPLICATION_JSON);
        HttpPost httpPost = new HttpPost("http://localhost:8080/servicerequest/api/v1/service/fundingRequest");
        httpPost.setHeader("Accept", ContentType.WILDCARD.getMimeType());
        httpPost.setEntity(strEntity);

        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                return Optional.of(UUID.fromString(EntityUtils.toString(entity)));
            }
        } catch (ParseException | IOException e) {
            log.error("accountRequest", e);
        }
        return Optional.empty();
    }

    public Optional<UUID> fundLoan(UUID accountId, BigDecimal amount, String description) {
        // Fund Loan
        int requestCount = 0;
        boolean loop = true;
        do {
            try {
                Optional<UUID> loanId = fundingRequest(accountId, amount, description);
                loop = loanId.isEmpty();
                if (!loop) {
                    return Optional.of(loanId.get());
                }
            } catch (Exception ex) {
                if (requestCount >= 3) {
                    log.warn("FUND LOAN EXCEPTION: {}", ex.getMessage());
                    loop = false;
                }
            }
            requestCount++;
            if (requestCount > 3) {
                loop = false;
            }
        } while (loop);
        return Optional.empty();
    }
}
