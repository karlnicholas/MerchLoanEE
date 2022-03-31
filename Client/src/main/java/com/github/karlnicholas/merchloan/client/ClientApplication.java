package com.github.karlnicholas.merchloan.client;

import com.github.karlnicholas.merchloan.client.component.*;
import com.github.karlnicholas.merchloan.client.process.LoanCycle;
import com.github.karlnicholas.merchloan.client.rest.LoanProcessQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
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
    private LoanStateComponent loanStateComponent;
    @Autowired
    private RequestStatusComponent requestStatusComponent;
    @Autowired
    private BusinessDateComponent businessDateComponent;
    @Autowired
    private LoanProcessQueue loanProcessQueue;
    private List<LoanCycle> loans;

    @EventListener(ApplicationReadyEvent.class)
    public void loadData(ApplicationReadyEvent event) {
        createLoanListeners();
        runBusinessDateThread();
    }

    private void createLoanListeners() {
        loans = new ArrayList<>();
        for ( int i =0; i < 5000; ++i ) {
            int plusDays = ThreadLocalRandom.current().nextInt(30);
//            int plusDays = 0;
            loans.add(new LoanCycle(creditComponent, accountComponent, loanComponent, loanStateComponent, requestStatusComponent, LocalDate.now().plusDays(plusDays), "Client " + i));
        }
    }

    private void runBusinessDateThread() {
        // do something
        new Thread(()->{
            try {
                LocalDate currentDate = LocalDate.now();
                LocalDate endDate = currentDate.plusYears(1).plusMonths(2);
                Thread.sleep(5000);
                int bdRetry;
                while ( currentDate.isBefore(endDate)) {
                    if ( loanProcessQueue.checkWorking() ) {
                        log.info("RestQueue still working {}", currentDate);
                        Thread.sleep(500);
                        continue;
                    }
                    bdRetry = 0;
                    if ( !businessDateComponent.updateBusinessDate(currentDate) ) {
                        if ( ++bdRetry > 10 ) {
                            log.error("Business date failed to update {}", currentDate);
                            return;
                        }
                        log.info("Business date not ready {}", currentDate);
                        Thread.sleep(500);
                        continue;
                    }
                    for( LoanCycle loan: loans) {
                        loan.cycle(loanProcessQueue, currentDate);
                    }
                    for( LoanCycle loan: loans) {
                        loan.updateState(currentDate);
                    }
                    if ( currentDate.getDayOfMonth() == 1 ) {
                        log.info("{}", currentDate);
                    }
                    currentDate = currentDate.plusDays(1);
                    Thread.sleep(500);
                }
                log.info("DATES FINISHED AT {}", currentDate);
//                threads.forEach(LoanCycleThread::showStatement);
            } catch (InterruptedException e) {
                log.error("Simulation thread interrupted", e);
                Thread.currentThread().interrupt();
            }
        }).start();
    }

}
