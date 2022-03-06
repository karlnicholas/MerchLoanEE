package com.github.karlnicholas.merchloan.client;

import com.github.karlnicholas.merchloan.client.component.AccountComponent;
import com.github.karlnicholas.merchloan.client.component.LoanComponent;
import com.github.karlnicholas.merchloan.client.component.LoanStatusComponent;
import com.github.karlnicholas.merchloan.client.process.LoanCycle;
import com.github.karlnicholas.merchloan.client.service.BusinessDateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@SpringBootApplication
@EnableScheduling
public class ClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class);
    }

    @Autowired
    private AccountComponent accountComponent;
    @Autowired
    private LoanComponent loanComponent;
    @Autowired
    private LoanStatusComponent loanStatusComponent;
    @Autowired
    private RestTemplate restTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void loadData(ApplicationReadyEvent event) {
        event.getApplicationContext().addApplicationListener(new LoanCycle(accountComponent, loanComponent, loanStatusComponent, restTemplate, "Client 1"));
//        event.getApplicationContext().addApplicationListener(new LoanCycle(accountComponent, loanComponent, loanStatusComponent, restTemplate, "Client 2"));
//        event.getApplicationContext().addApplicationListener(new LoanCycle(accountComponent, loanComponent, loanStatusComponent, restTemplate, "Client 3"));
        // do something
    }

}
