package com.github.karlnicholas.merchloan.client.service;

import com.github.karlnicholas.merchloan.client.process.BusinessDateEvent;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class BusinessDateService {
    private final ApplicationEventPublisher applicationEventPublisher;
    private LocalDate currentDate;

    public BusinessDateService(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.currentDate = LocalDate.now();
    }

    @Scheduled( initialDelay = 5000, fixedDelay = 1000)
    public void businessDateScheduler() {
        publishCustomEvent(currentDate);
        currentDate = currentDate.plusDays(1);
    }
    public void publishCustomEvent(final LocalDate message) {
        BusinessDateEvent businessDateEvent = new BusinessDateEvent(this, message);
        applicationEventPublisher.publishEvent(businessDateEvent);
    }

}
