package com.github.karlnicholas.merchloan.jms.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

@Configuration
@Slf4j
public class MQConnectionConfig {
    @Value("${rabbitmq.host:localhost}")
    private String host;
    @Value("${rabbitmq.port:61616}")
    private Integer port;
    @Value("${rabbitmq.virtual-host:/}")
    private String virtualHost;

    @Bean
    public ConnectionFactory getConnectionFactory() {
        return new ActiveMQConnectionFactory("tcp://" + host + ":" + port);
    }

    @Bean
    public Connection getConnection(ConnectionFactory connectionFactory) throws InterruptedException, JMSException {
        int retryCount = 0;
        while (retryCount < 3) {
            try {
                return connectionFactory.createConnection();
            } catch (JMSException e) {
                Thread.sleep(3000);
                // apply retry logic
                retryCount++;
                if (retryCount >= 3) {
                    throw e;
                }
                throw new RuntimeException(e);
            }
        }
        return null;
    }

}