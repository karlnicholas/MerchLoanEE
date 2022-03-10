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
        event.getApplicationContext().addApplicationListener(new LoanCycle(creditComponent, accountComponent, loanComponent, closeComponent, loanStateComponent, requestStatusComponent, LocalDate.now(), "Client 1"));
//        event.getApplicationContext().addApplicationListener(new LoanCycle(creditComponent, accountComponent, loanComponent, closeComponent, loanStateComponent, requestStatusComponent, LocalDate.now().plusDays(1), "Client 2"));
//        event.getApplicationContext().addApplicationListener(new LoanCycle(creditComponent, accountComponent, loanComponent, closeComponent, loanStateComponent, requestStatusComponent, LocalDate.now().plusDays(2), "Client 3"));
//        event.getApplicationContext().addApplicationListener(new LoanCycle(creditComponent, accountComponent, loanComponent, closeComponent, loanStateComponent, requestStatusComponent, LocalDate.now().plusDays(3), "Client 4"));
//        event.getApplicationContext().addApplicationListener(new LoanCycle(creditComponent, accountComponent, loanComponent, closeComponent, loanStateComponent, requestStatusComponent, LocalDate.now().plusDays(4), "Client 5"));
//        event.getApplicationContext().addApplicationListener(new LoanCycle(creditComponent, accountComponent, loanComponent, closeComponent, loanStateComponent, requestStatusComponent, LocalDate.now().plusDays(5), "Client 6"));
//        event.getApplicationContext().addApplicationListener(new LoanCycle(creditComponent, accountComponent, loanComponent, closeComponent, loanStateComponent, requestStatusComponent, LocalDate.now().plusDays(6), "Client 7"));
//        event.getApplicationContext().addApplicationListener(new LoanCycle(creditComponent, accountComponent, loanComponent, closeComponent, loanStateComponent, requestStatusComponent, LocalDate.now().plusDays(7), "Client 8"));
//        event.getApplicationContext().addApplicationListener(new LoanCycle(creditComponent, accountComponent, loanComponent, closeComponent, loanStateComponent, requestStatusComponent, LocalDate.now().plusDays(8), "Client 9"));
//        event.getApplicationContext().addApplicationListener(new LoanCycle(creditComponent, accountComponent, loanComponent, closeComponent, loanStateComponent, requestStatusComponent, LocalDate.now().plusDays(9), "Client 10"));
//        event.getApplicationContext().addApplicationListener(new LoanCycle(creditComponent, accountComponent, loanComponent, closeComponent, loanStateComponent, requestStatusComponent, LocalDate.now().plusDays(10), "Client 11"));
//        event.getApplicationContext().addApplicationListener(new LoanCycle(creditComponent, accountComponent, loanComponent, closeComponent, loanStateComponent, requestStatusComponent, LocalDate.now().plusDays(11), "Client 12"));
//        event.getApplicationContext().addApplicationListener(new LoanCycle(creditComponent, accountComponent, loanComponent, closeComponent, loanStateComponent, requestStatusComponent, LocalDate.now().plusDays(12), "Client 13"));
//        event.getApplicationContext().addApplicationListener(new LoanCycle(creditComponent, accountComponent, loanComponent, closeComponent, loanStateComponent, requestStatusComponent, LocalDate.now().plusDays(13), "Client 14"));
//        event.getApplicationContext().addApplicationListener(new LoanCycle(creditComponent, accountComponent, loanComponent, closeComponent, loanStateComponent, requestStatusComponent, LocalDate.now().plusDays(14), "Client 15"));
//        event.getApplicationContext().addApplicationListener(new LoanCycle(creditComponent, accountComponent, loanComponent, closeComponent, loanStateComponent, requestStatusComponent, LocalDate.now().plusDays(15), "Client 16"));
//        event.getApplicationContext().addApplicationListener(new LoanCycle(creditComponent, accountComponent, loanComponent, closeComponent, loanStateComponent, requestStatusComponent, LocalDate.now().plusDays(16), "Client 17"));
//        event.getApplicationContext().addApplicationListener(new LoanCycle(creditComponent, accountComponent, loanComponent, closeComponent, loanStateComponent, requestStatusComponent, LocalDate.now().plusDays(17), "Client 18"));
//        event.getApplicationContext().addApplicationListener(new LoanCycle(creditComponent, accountComponent, loanComponent, closeComponent, loanStateComponent, requestStatusComponent, LocalDate.now().plusDays(18), "Client 19"));
//        event.getApplicationContext().addApplicationListener(new LoanCycle(creditComponent, accountComponent, loanComponent, closeComponent, loanStateComponent, requestStatusComponent, LocalDate.now().plusDays(19), "Client 20"));
//        event.getApplicationContext().addApplicationListener(new LoanCycle(creditComponent, accountComponent, loanComponent, closeComponent, loanStateComponent, requestStatusComponent, LocalDate.now().plusDays(20), "Client 21"));
//        event.getApplicationContext().addApplicationListener(new LoanCycle(creditComponent, accountComponent, loanComponent, closeComponent, loanStateComponent, requestStatusComponent, LocalDate.now().plusDays(21), "Client 22"));
//        event.getApplicationContext().addApplicationListener(new LoanCycle(creditComponent, accountComponent, loanComponent, closeComponent, loanStateComponent, requestStatusComponent, LocalDate.now().plusDays(22), "Client 23"));
//        event.getApplicationContext().addApplicationListener(new LoanCycle(creditComponent, accountComponent, loanComponent, closeComponent, loanStateComponent, requestStatusComponent, LocalDate.now().plusDays(23), "Client 24"));
//        event.getApplicationContext().addApplicationListener(new LoanCycle(creditComponent, accountComponent, loanComponent, closeComponent, loanStateComponent, requestStatusComponent, LocalDate.now().plusDays(24), "Client 25"));
//        event.getApplicationContext().addApplicationListener(new LoanCycle(creditComponent, accountComponent, loanComponent, closeComponent, loanStateComponent, requestStatusComponent, LocalDate.now().plusDays(25), "Client 26"));
//        event.getApplicationContext().addApplicationListener(new LoanCycle(creditComponent, accountComponent, loanComponent, closeComponent, loanStateComponent, requestStatusComponent, LocalDate.now().plusDays(26), "Client 27"));
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
