package com.github.karlnicholas.merchloan.businessdate.businessdate.service;

import com.github.karlnicholas.merchloan.businessdate.businessdate.repository.BusinessDateRepository;
import com.github.karlnicholas.merchloan.businessdate.businessdate.model.BusinessDate;
import com.github.karlnicholas.merchloan.jms.message.RabbitMqSender;
import com.github.karlnicholas.merchloan.jmsmessage.BillingCycle;
import com.github.karlnicholas.merchloan.redis.component.RedisComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BusinessDateService {
    private final RedisComponent redisComponent;
    private final BusinessDateRepository businessDateRepository;
    private final RabbitMqSender rabbitMqSender;

    public BusinessDateService(RedisComponent redisComponent, BusinessDateRepository businessDateRepository, RabbitMqSender rabbitMqSender) {
        this.redisComponent = redisComponent;
        this.businessDateRepository = businessDateRepository;
        this.rabbitMqSender = rabbitMqSender;
    }

    public BusinessDate updateBusinessDate(LocalDate businessDate) {
        return businessDateRepository.findById(1L).map(pr->{
            BusinessDate priorBusinessDate = BusinessDate.builder().businessDate(pr.getBusinessDate()).build();
            businessDateRepository.save(BusinessDate.builder().id(1L).businessDate(businessDate).build());
            redisComponent.updateBusinessDate(businessDate);
            return priorBusinessDate;
        }).orElseThrow();
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
                Thread.sleep(1000);
                Boolean requestPending = (Boolean) rabbitMqSender.servicerequestCheckRequest(priorBusinessDate.getBusinessDate());
                if ( !requestPending.booleanValue() )
                    waiting = false;
            } catch (InterruptedException e) {
                log.error("Wait for prior day completion exception: {}", e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
        List<BillingCycle> loansToCycle = (List<BillingCycle>) rabbitMqSender.acccountQueryLoansToCycle(priorBusinessDate.getBusinessDate());
        redisComponent.setLoansToCycle(priorBusinessDate.getBusinessDate(), loansToCycle.stream().map(BillingCycle::getLoanId).collect(Collectors.toList()));
        loansToCycle.forEach(rabbitMqSender::serviceRequestBillLoan);
    }
}
