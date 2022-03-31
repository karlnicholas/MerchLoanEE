package com.github.karlnicholas.merchloan.client.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.karlnicholas.merchloan.apimessage.message.CloseRequest;
import com.github.karlnicholas.merchloan.dto.LoanDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class LoanStateComponent {
    private final ObjectMapper objectMapper;

    public LoanStateComponent(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private Optional<LoanDto> loanStatus(UUID loanId) {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet("http://localhost:8090/api/query/loan/" + loanId.toString());
            httpGet.setHeader("Accept", ContentType.WILDCARD.getMimeType());
            try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
                HttpEntity entity = response.getEntity();
                return Optional.of(objectMapper.readValue(EntityUtils.toString(entity), LoanDto.class));
            } catch (ParseException e) {
                log.error("accountRequest", e);
            }
        } catch (IOException e) {
            log.error("accountRequest", e);
        }
        return Optional.empty();
//        HttpHeaders headers = new HttpHeaders();
//        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
//        return restTemplate.exchange("http://localhost:8090/api/query/loan/{loanId}", HttpMethod.GET, null, LoanDto.class, loanId);
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
