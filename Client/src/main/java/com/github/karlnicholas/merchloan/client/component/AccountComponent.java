package com.github.karlnicholas.merchloan.client.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.karlnicholas.merchloan.apimessage.message.AccountRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
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
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Component
@Slf4j
public class AccountComponent {
    private final ObjectMapper objectMapper;
    private final CloseableHttpClient httpclient;

    public AccountComponent(PoolingHttpClientConnectionManager connManager) {
        this.objectMapper = new ObjectMapper().findAndRegisterModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        httpclient = HttpClients.custom().setConnectionManager(connManager).build();
    }

    private Optional<UUID> accountRequest(String customer) throws HttpException, IOException, ExecutionException, InterruptedException {
        String strJson = objectMapper.writeValueAsString(new AccountRequest(customer));
        StringEntity strEntity = new StringEntity(strJson, ContentType.APPLICATION_JSON);
        HttpPost httpPost = new HttpPost("http://localhost:8080/servicerequest/api/v1/service/accountRequest");
        httpPost.setHeader("Accept", ContentType.WILDCARD.getMimeType());
        httpPost.setEntity(strEntity);

        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                return Optional.of(UUID.fromString(EntityUtils.toString(entity)));
            }
        } catch (ParseException e) {
            log.error("accountRequest", e);
        }
        return Optional.empty();
    }

    public Optional<UUID> createAccount(String customer) throws HttpException, IOException, ExecutionException, InterruptedException {
        // Open Account
        return accountRequest(customer);
    }
}
