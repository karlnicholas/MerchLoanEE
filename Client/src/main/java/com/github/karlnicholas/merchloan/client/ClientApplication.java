package com.github.karlnicholas.merchloan.client;

import com.github.karlnicholas.merchloan.client.component.*;
import com.github.karlnicholas.merchloan.client.process.BusinessDateEvent;
import com.github.karlnicholas.merchloan.client.process.LoanCycle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;

import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

@SpringBootApplication
@Slf4j
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
    @Autowired
    private BusinessDateComponent businessDateComponent;

    @EventListener(ApplicationReadyEvent.class)
    public void loadData(ApplicationReadyEvent event) {
        createLoanListeners(event);
        runBusinessDateLoop();
    }

    private void createLoanListeners(ApplicationReadyEvent event) {
        for ( int i =0; i < 1; ++i ) {
            int plusDays = 0;
            event.getApplicationContext().addApplicationListener(new LoanCycle(creditComponent, accountComponent, loanComponent, closeComponent, loanStateComponent, requestStatusComponent, LocalDate.now().plusDays(plusDays), "Client " + i));
        }
    }

    private void runBusinessDateLoop() {
        // do something
        new Thread(()->{
            try {
                LocalDate currentDate = LocalDate.now();
                LocalDate endDate = currentDate.plusYears(1).plusMonths(1).plusDays(1);
                Thread.sleep(5000);
                while ( currentDate.isBefore(endDate)) {
                    if ( !businessDateComponent.updateBusinessDate(currentDate) ) {
                        log.error("Business date failed to update");
                        return;
                    }
                    BusinessDateEvent businessDateEvent = new BusinessDateEvent(this, currentDate);
                    applicationEventPublisher.publishEvent(businessDateEvent);
                    currentDate = currentDate.plusDays(1);
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                log.error("Simulation thread interrupted", e);
                Thread.currentThread().interrupt();
            }
        }).start();
    }

}
