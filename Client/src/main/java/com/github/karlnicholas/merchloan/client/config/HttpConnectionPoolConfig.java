package com.github.karlnicholas.merchloan.client.config;

import org.apache.http.HttpHost;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpConnectionPoolConfig {
    private static final String HOST = "localhost";
    @Bean
    public PoolingHttpClientConnectionManager getPoolingHttpClientConnectionManager() {
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setMaxPerRoute(new HttpRoute(new HttpHost(HOST, 8080)), 100);
        connManager.setMaxPerRoute(new HttpRoute(new HttpHost(HOST, 8090)), 200);
        connManager.setMaxPerRoute(new HttpRoute(new HttpHost(HOST, 8100)), 1);
        connManager.setMaxTotal(301);

        return connManager;
    }
}
