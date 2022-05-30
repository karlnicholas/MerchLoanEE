package com.github.karlnicholas.merchloan.client.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.karlnicholas.merchloan.dto.LoanDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class LoanStateComponent {
    private final ObjectMapper objectMapper;
    private final CloseableHttpClient httpclient;

    public LoanStateComponent(PoolingHttpClientConnectionManager connManager) {
        this.objectMapper = new ObjectMapper().findAndRegisterModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        httpclient = HttpClients.custom().setConnectionManager(connManager).build();
    }

    private Optional<LoanDto> loanStatus(UUID loanId) {
        HttpGet httpGet = new HttpGet("http://localhost:8080/query/api/query/loan/" + loanId.toString());
        httpGet.setHeader("Accept", ContentType.WILDCARD.getMimeType());
        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                return Optional.of(objectMapper.readValue(EntityUtils.toString(entity), LoanDto.class));
            }
        } catch (ParseException | IOException e) {
            log.error("accountRequest", e);
        }
        return Optional.empty();
    }

    public Optional<LoanDto> checkLoanStatus(UUID loanId) {
        // Return Loan State
        boolean loop = true;
        int requestCount = 0;
        do {
            try {
                Optional<LoanDto> loanDtoResp = loanStatus(loanId);
                loop = loanDtoResp.isEmpty();
                if (!loop) {
                    return Optional.of(loanDtoResp.get());
                }
            } catch (Exception ex) {
                if (requestCount == 3) {
                    log.warn("LOAN STATE EXCEPTION: {}", ex.getMessage());
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
