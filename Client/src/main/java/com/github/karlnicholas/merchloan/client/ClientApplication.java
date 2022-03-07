package com.github.karlnicholas.merchloan.client;

import com.github.karlnicholas.merchloan.client.component.*;
import com.github.karlnicholas.merchloan.client.process.LoanCycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

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
    private CloseComponent closeComponent;
    @Autowired
    private LoanStatusComponent loanStatusComponent;
    @Autowired
    private RequestStatusComponent requestStatusComponent;
    @Autowired
    private RestTemplate restTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void loadData(ApplicationReadyEvent event) {
        event.getApplicationContext().addApplicationListener(new LoanCycle(accountComponent, loanComponent, closeComponent, requestStatusComponent, loanStatusComponent, restTemplate, "Client 1"));
//        event.getApplicationContext().addApplicationListener(new LoanCycle(accountComponent, loanComponent, loanStatusComponent, restTemplate, "Client 2"));
//        event.getApplicationContext().addApplicationListener(new LoanCycle(accountComponent, loanComponent, loanStatusComponent, restTemplate, "Client 3"));
        // do something
    }

}
