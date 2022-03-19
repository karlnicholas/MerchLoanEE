package com.github.karlnicholas.merchloan.client;

import com.github.karlnicholas.merchloan.client.component.*;
import com.github.karlnicholas.merchloan.client.process.BusinessDateMonitor;
import com.github.karlnicholas.merchloan.client.process.LoanCycleThread;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
    private BusinessDateMonitor businessDateMonitor;
    private List<LoanCycleThread> threads;

    @EventListener(ApplicationReadyEvent.class)
    public void loadData(ApplicationReadyEvent event) {
        businessDateMonitor = new BusinessDateMonitor();
        createLoanListeners();
        runBusinessDateThread();
    }

    private void createLoanListeners() {
        threads = new ArrayList<>();
        for ( int i =0; i < 5; ++i ) {
            int plusDays = ThreadLocalRandom.current().nextInt(30);
            threads.add(new LoanCycleThread(creditComponent, accountComponent, loanComponent, closeComponent, loanStateComponent, requestStatusComponent, businessDateMonitor, LocalDate.now().plusDays(plusDays), "Client " + i));
        }
        threads.forEach(Thread::start);
    }

    private void runBusinessDateThread() {
        // do something
        new Thread(()->{
            try {
                LocalDate currentDate = LocalDate.now();
                LocalDate endDate = currentDate.plusYears(1).plusMonths(2);
                Thread.sleep(5000);
                while ( currentDate.isBefore(endDate)) {
                    if ( !businessDateComponent.updateBusinessDate(currentDate) ) {
                        log.error("Business date failed to update");
                        return;
                    }
                    businessDateMonitor.newDate(currentDate);
                    if ( currentDate.getDayOfMonth() == 1 ) {
                        log.info("{}", currentDate);
                    }
                    currentDate = currentDate.plusDays(1);
                    Thread.sleep(500);
                }
                businessDateMonitor.newDate(null);
                log.info("{}", currentDate);
                threads.forEach(LoanCycleThread::showStatement);
            } catch (InterruptedException e) {
                log.error("Simulation thread interrupted", e);
                Thread.currentThread().interrupt();
            }
        }).start();
    }

}
