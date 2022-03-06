package com.github.karlnicholas.merchloan.client.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate getRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(converter());
        return restTemplate;
    }
    @Bean
    public HttpMessageConverter<UUID> converter() {

        return new HttpMessageConverter<>() {
            @Override
            public boolean canRead(Class<?> clazz, MediaType mediaType) {
                return clazz == UUID.class;
            }

            @Override
            public boolean canWrite(Class<?> clazz, MediaType mediaType) {
                return clazz == UUID.class;
            }

            @Override
            public List<MediaType> getSupportedMediaTypes() {
                return Arrays.asList(MediaType.TEXT_PLAIN);
            }

            @Override
            public UUID read(Class<? extends UUID> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
                return UUID.fromString(new String(inputMessage.getBody().readAllBytes()));
            }

            @Override
            public void write(UUID uuid, MediaType contentType, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
                outputMessage.getBody().write(uuid.toString().getBytes());
            }
        };
    }

}
