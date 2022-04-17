package com.github.karlnicholas.merchloan.client.config;

import org.apache.http.HttpHost;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpConnectionPoolConfig {
    @Bean
    public PoolingHttpClientConnectionManager getPoolingHttpClientConnectionManager() {
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setMaxPerRoute(new HttpRoute(new HttpHost("localhost", 8080)), 100);
        connManager.setMaxPerRoute(new HttpRoute(new HttpHost("localhost", 8090)), 200);
        connManager.setMaxPerRoute(new HttpRoute(new HttpHost("localhost", 8100)), 1);
        connManager.setMaxTotal(301);

        return connManager;
//        CloseableHttpClient client
//                = HttpClients.custom().setConnectionManager(poolingConnManager)
//                .build();
//        client.execute(new HttpGet("/"));
//        assertTrue(poolingConnManager.getTotalStats().getLeased() == 1);

    }
}
