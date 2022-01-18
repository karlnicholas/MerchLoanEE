package com.github.karlnicholas.merchloan.businessdate.businessdate.service;

import com.github.karlnicholas.merchloan.businessdate.businessdate.repository.BusinessDateRepository;
import com.github.karlnicholas.merchloan.businessdate.businessdate.model.BusinessDate;
import com.github.karlnicholas.merchloan.jms.message.RabbitMqSender;
import com.github.karlnicholas.merchloan.redis.component.RedisComponent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BusinessDateService {
    private final RedisComponent redisComponent;
    private final BusinessDateRepository businessDateRepository;
    private final RabbitMqSender rabbitMqSender;

    public BusinessDateService(RedisComponent redisComponent, BusinessDateRepository businessDateRepository, RabbitMqSender rabbitMqSender) {
        this.redisComponent = redisComponent;
        this.businessDateRepository = businessDateRepository;
        this.rabbitMqSender = rabbitMqSender;
    }

    public void updateBusinessDate(LocalDate businessDate) {
        Optional<BusinessDate> currentBusinessDate = businessDateRepository.findById(1L);
        businessDateRepository.save(BusinessDate.builder().id(1L).businessDate(businessDate).build());
        redisComponent.updateBusinessDate(businessDate);
        if ( currentBusinessDate.isPresent()) {
            startBillingCycle(currentBusinessDate.get());
        }
    }

    public void initializeBusinessDate() {
        BusinessDate businessDate = businessDateRepository.findById(1L).orElse(BusinessDate.builder().id(1L).businessDate(LocalDate.now()).build());
        businessDateRepository.save(businessDate);
        redisComponent.updateBusinessDate(businessDate.getBusinessDate());
    }

    @Async
    public void startBillingCycle(BusinessDate priorBusinessDate) {
        boolean waiting = true;
        while(waiting) {
            try {
                Thread.sleep(5000);
                Boolean completed = (Boolean) rabbitMqSender.acccountCheckRequests(priorBusinessDate.getBusinessDate());
                if ( completed )
                    waiting = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
                waiting = false;

            }
        }
        List<UUID> loansToCycles = (List<UUID>) rabbitMqSender.acccountLoansToCycle(priorBusinessDate.getBusinessDate());
        loansToCycles.forEach(rabbitMqSender::serviceRequestBillLoan);

    }
}
