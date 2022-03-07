package com.github.karlnicholas.merchloan.client;

import com.github.karlnicholas.merchloan.client.component.*;
import com.github.karlnicholas.merchloan.client.process.BusinessDateEvent;
import com.github.karlnicholas.merchloan.client.process.LoanCycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;

import java.time.LocalDate;

@SpringBootApplication
public class ClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class);
    }

    @Autowired
    private AccountComponent accountComponent;
    @Autowired
    private LoanComponent loanComponent;
    @Autowired
    private CreditComponent creditComponent;
    @Autowired
    private CloseComponent closeComponent;
    @Autowired
    private LoanStateComponent loanStateComponent;
    @Autowired
    private RequestStatusComponent requestStatusComponent;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @EventListener(ApplicationReadyEvent.class)
    public void loadData(ApplicationReadyEvent event) {
        event.getApplicationContext().addApplicationListener(new LoanCycle(creditComponent, accountComponent, loanComponent, closeComponent, loanStateComponent, "Client 1"));
        // do something
        new Thread(()->{
            try {
                LocalDate currentDate = LocalDate.now();
                LocalDate endDate = currentDate.plusYears(1).plusDays(1);
                Thread.sleep(5000);
                while ( currentDate.isBefore(endDate)) {
                    BusinessDateEvent businessDateEvent = new BusinessDateEvent(this, currentDate);
                    applicationEventPublisher.publishEvent(businessDateEvent);
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }).start();
    }

}
