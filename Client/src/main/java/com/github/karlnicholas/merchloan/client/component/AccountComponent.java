package com.github.karlnicholas.merchloan.client.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.karlnicholas.merchloan.apimessage.message.AccountRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.ConnectionRequest;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class AccountComponent {
    private final ObjectMapper objectMapper;
    private final PoolingHttpClientConnectionManager connManager;

    public AccountComponent(PoolingHttpClientConnectionManager connManager) {
        this.objectMapper = new ObjectMapper().findAndRegisterModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        this.connManager = connManager;
    }

    private Optional<UUID> accountRequest(String customer) throws HttpException, IOException, ExecutionException, InterruptedException {
//        BasicHttpClientConnectionManager basicConnManager = new BasicHttpClientConnectionManager();
//        HttpClientContext context = HttpClientContext.create();
//        HttpRoute route = new HttpRoute(new HttpHost("localhost", 8080));
//        ConnectionRequest connRequest = basicConnManager.requestConnection(route, null);
//        HttpClientConnection conn = connRequest.get(10, TimeUnit.SECONDS);
//        basicConnManager.connect(conn, route, 1000, context);
//        basicConnManager.routeComplete(conn, route, context);
//
//        HttpRequestExecutor exeRequest = new HttpRequestExecutor();
//        context.setTargetHost((new HttpHost("localhost", 8080)));
//        String strJson = objectMapper.writeValueAsString(new AccountRequest(customer));
//        HttpPost httpPost = new HttpPost("http://localhost:8080/api/v1/service/accountRequest");
//        StringEntity strEntity = new StringEntity(strJson, ContentType.APPLICATION_JSON);
//        httpPost.setHeader("Accept", ContentType.WILDCARD.getMimeType());
////            httpPost.setHeader("Content-type", ContentType.APPLICATION_JSON.getMimeType());
//        httpPost.setEntity(strEntity);
//        HttpResponse response = exeRequest.execute(httpPost, conn, context);
//        basicConnManager.releaseConnection(conn, null, 1, TimeUnit.MINUTES);
//        HttpEntity entity = response.getEntity();
//        if ( response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
//            return Optional.of(UUID.fromString(EntityUtils.toString(entity)));
//        } else {
//            return Optional.empty();
//        }
//        basicConnManager.releaseConnection(conn, null, 1, TimeUnit.SECONDS);

        CloseableHttpClient httpclient = HttpClients.custom().setConnectionManager(connManager).build();
            String strJson = objectMapper.writeValueAsString(new AccountRequest(customer));
            StringEntity strEntity = new StringEntity(strJson, ContentType.APPLICATION_JSON);
            HttpPost httpPost = new HttpPost("http://localhost:8080/api/v1/service/accountRequest");
            httpPost.setHeader("Accept", ContentType.WILDCARD.getMimeType());
//            httpPost.setHeader("Content-type", ContentType.APPLICATION_JSON.getMimeType());
            httpPost.setEntity(strEntity);

            try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
                if ( response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    HttpEntity entity = response.getEntity();
                    return Optional.of(UUID.fromString(EntityUtils.toString(entity)));
                }
            } catch (ParseException e) {
                log.error("accountRequest", e);
            }
//        } catch (IOException e) {
//            log.error("accountRequest", e);
//        }
        return Optional.empty();
//        return restTemplate.exchange("http://localhost:8080/api/v1/service/accountRequest", HttpMethod.POST, request, UUID.class);
    }

    public Optional<UUID> createAccount(String customer) throws HttpException, IOException, ExecutionException, InterruptedException {
        // Open Account
        return accountRequest(customer);
    }
}
