package com.github.karlnicholas.merchloan.servicerequest.service;

import com.github.karlnicholas.merchloan.apimessage.message.ServiceRequestMessage;
import com.github.karlnicholas.merchloan.servicerequest.repository.ServiceRequestRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RetryScheduleService {
    private final ServiceRequestRepository serviceRequestRepository;
    private final RetryService retryService;

    public RetryScheduleService(ServiceRequestRepository serviceRequestRepository, RetryService retryService) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.retryService = retryService;
    }

    @Scheduled(initialDelay = 10000, fixedDelay = 10000)
    public void retryService() {
        serviceRequestRepository.findByStatus(ServiceRequestMessage.STATUS.ERROR)
                .forEach(sr -> {
                    sr.setRetryCount(sr.getRetryCount() + 1);
                    if (sr.getRetryCount() > 3) {
                        sr.setStatus(ServiceRequestMessage.STATUS.FAILURE);
                        sr.setStatusMessage("Exceeded Retry Count");
                    }
                    serviceRequestRepository.save(sr);
                    if (sr.getRetryCount() <= 3) {
                        retryService.retryServiceRequest(sr);
                    }
                });
    }

    @Scheduled(initialDelay = 10000, fixedDelay = 10000)
    public void waitingService() {

    }


}
