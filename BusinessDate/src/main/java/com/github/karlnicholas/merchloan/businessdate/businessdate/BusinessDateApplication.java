package com.github.karlnicholas.merchloan.businessdate.businessdate;

import com.github.karlnicholas.merchloan.businessdate.businessdate.service.BusinessDateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@SpringBootApplication(scanBasePackages = "com.github.karlnicholas.merchloan")
public class BusinessDateApplication {

    public static void main(String[] args) {
        SpringApplication.run(BusinessDateApplication.class, args);
    }

    @Autowired
    private BusinessDateService businessDateService;

    @EventListener(ContextRefreshedEvent.class)
    public void initialize() {
        businessDateService.initializeBusinessDate();
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
