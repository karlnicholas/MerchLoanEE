package com.github.karlnicholas.merchloan.client.component;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Component
@Slf4j
public class BusinessDateComponent {

    private Optional<Integer> postBusinessDate(LocalDate businessDate) {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            String date = businessDate.format(DateTimeFormatter.ISO_DATE);
            StringEntity strEntity = new StringEntity(date, ContentType.TEXT_PLAIN);
            HttpPost httpPost = new HttpPost("http://localhost:8100/api/businessdate");
            httpPost.setHeader("Accept", ContentType.WILDCARD.getMimeType());
//            httpPost.setHeader("Content-type", ContentType.TEXT_PLAIN.getMimeType());
            httpPost.setEntity(strEntity);

            try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
                return Optional.of(response.getStatusLine().getStatusCode());
            } catch (ParseException e) {
                log.error("accountRequest", e);
            }
        } catch (IOException e) {
            log.error("accountRequest", e);
        }
        return Optional.empty();
//        HttpHeaders headers = new HttpHeaders();
//        headers.setAccept(Collections.singletonList(MediaType.ALL));
//        headers.setContentType(MediaType.TEXT_PLAIN);
//        HttpEntity<String> request = new HttpEntity<>(businessDate.format(DateTimeFormatter.ISO_DATE), headers);
//        return restTemplate.exchange("http://localhost:8100/api/businessdate", HttpMethod.POST, request, Void.class);
    }

    private Optional<Boolean> checkStillProcessing() {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet("http://localhost:8090/api/query/checkrequests");
            httpGet.setHeader("Accept", ContentType.WILDCARD.getMimeType());

            try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
                return Optional.of(Boolean.valueOf(EntityUtils.toString(response.getEntity())));
            } catch (ParseException e) {
                log.error("accountRequest", e);
            }
        } catch (IOException e) {
            log.error("accountRequest", e);
        }
        return Optional.empty();
//        HttpHeaders headers = new HttpHeaders();
//        headers.setAccept(Collections.singletonList(MediaType.ALL));
//        return restTemplate.exchange("http://localhost:8090/api/query/checkrequests", HttpMethod.GET, null, Boolean.class);
    }

    public boolean updateBusinessDate(LocalDate localDate) {
        int requestCount = 0;
        boolean loop = true;
        do {
            try {
                Optional<Boolean> stillProcessingResp = checkStillProcessing();
                if ( stillProcessingResp.isEmpty() || stillProcessingResp.get().booleanValue() == true) {
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
                Optional<Integer> requestResponse = postBusinessDate(localDate);
                loop = requestResponse.isEmpty() || requestResponse.get() != 200;
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
